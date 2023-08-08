package product;


import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cacheable
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;
    public String description;

    @CreationTimestamp
    public ZonedDateTime createdAt;

    @UpdateTimestamp
    public ZonedDateTime updatedAt;

    public static Uni<Product> findByProductId(Long id) {
        return findById(id);
    }

    public static Uni<Product> updateProduct(Long id, Product product) {
        return Panache
                .withTransaction(() -> findByProductId(id)
                        .onItem().ifNotNull()
                        .transform(entity -> {
                            entity.description = product.description;
                            entity.title = product.title;
                            return entity;
                        })
                        .onFailure().recoverWithNull());
    }

    public static Uni<Product> addProduct(Product product) {
        return Panache
                .withTransaction(product::persist)
                .replaceWith(product)
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .transform(t -> new IllegalStateException(t));
    }

    public static Uni<List<Product>> getAllProducts() {
        return Product
                .listAll(Sort.by("createdAt"))
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .recoverWithUni(Uni.createFrom().<List<io.quarkus.hibernate.reactive.panache.PanacheEntityBase>>item(Collections.EMPTY_LIST));

    }

    public static Uni<Boolean> deleteProduct(Long id) {
        return Panache.withTransaction(() -> deleteById(id));
    }

    public String toString() {
        return this.getClass().getSimpleName() + "<" + this.id + ">";
    }

}

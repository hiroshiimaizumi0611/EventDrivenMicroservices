package com.develop.ProductsService.core.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="product_lookup")
public class ProductLookupEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2788007460547645663L;

    @Id
    private String productId;

    @Column(unique=true)
    private String title;
}

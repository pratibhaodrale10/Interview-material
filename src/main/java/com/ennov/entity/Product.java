package com.ennov.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

	@JsonIgnore
	private Long product_id;
	private String product_line;
	private String product_brand;
	private String product_generic;
	private BigDecimal product_price;
	private String prodDiff;

}

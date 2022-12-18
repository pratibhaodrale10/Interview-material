package com.ennov.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ennov.entity.Product;
import com.ennov.entity.ProductResponseEntity;

@RestController
public class ProductController {
	final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	String file = "E:\\pratibha\\Interviews\\ennov\\product.csv";
	List<Product> prodList = new ArrayList<>();
	BigDecimal proPriceReq, proPrice;

//	readCSV() method will read provided CSV from local directory
	@RequestMapping(path = "/readCSV", method = RequestMethod.GET)
	public ResponseEntity<String> readCSV() {
		LOGGER.info("ProductController: readCSV()");
		List<Product> productsFromCSV = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file));
				CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',').withHeader().parse(br);) {
			for (CSVRecord record : parser) {
				Product product = new Product();
				if (record.get("product_id") != null && !record.get("product_id").isEmpty()
						&& record.get("product_line") != null && !record.get("product_line").isEmpty()
						&& record.get("product_brand") != null && !record.get("product_brand").isEmpty()
						&& record.get("product_generic") != null && !record.get("product_generic").isEmpty()
						&& record.get("product_price") != null && !record.get("product_price").isEmpty()) {
					product.setProduct_id(Long.parseLong(record.get("product_id")));
					product.setProduct_line(record.get("product_line"));
					product.setProduct_brand(record.get("product_brand"));
					product.setProduct_generic(record.get("product_generic"));
					product.setProduct_price(new BigDecimal(String.valueOf(record.get("product_price"))));
					productsFromCSV.add(product);
				}
			}
			prodList = productsFromCSV;
			LOGGER.info("readCSV():: product count from CSV - " + prodList.size());
		} catch (Exception e) {
			LOGGER.info("readCSV(): Exception when reading CSV file:: " + e.getMessage());
		}
		return new ResponseEntity<String>("Read the provided CSV", HttpStatus.OK);
	}

//	filterProducts() will filter products by product_line, product_brand, product_generic and product_price
//	filter by product_line and sum of the filtered products
//	differences between the ‘product generics’ and the ‘product lines’ for all products
	@RequestMapping(path = "/filterProducts", method = RequestMethod.GET)
	public ResponseEntity<ProductResponseEntity> filterProducts(@RequestParam Map<String, String> params) {
		LOGGER.info("ProductController: filterProducts()");
		ProductResponseEntity responseEntity = new ProductResponseEntity();
		if (!params.isEmpty()) {
			Set<String> keys = params.keySet();
			List<Product> products = prodList;
			Iterator<String> itr = keys.iterator();
			BigDecimal sum = new BigDecimal(0.0);
			while (itr.hasNext()) {
				String key = itr.next();
				if (key.equals("product_line")) {
					products = filterByLine(params.get(key), products);
					if (!itr.hasNext()) {
						sum = sumOfFilteredProd(params.get(key), products);
						LOGGER.info("filterProducts(): sum of filtered products(filtred by product_line):: " + sum);
					}
				}
				if (key.equals("product_brand")) {
					products = filterByBrand(params.get(key), products);
				}
				if (key.equals("product_price")) {
					products = filterByPrice(params.get(key), products);
				}
			}
			responseEntity.setTotalSum(sum);
			responseEntity.setProducts(products);
			LOGGER.info("filterProducts(): size of filtered products:: " + products.size());
		}
		return new ResponseEntity<ProductResponseEntity>(responseEntity, HttpStatus.OK);
	}

	private List<Product> filterByLine(String Key, List<Product> products) {
		LOGGER.info("ProductController: filterByLine()");
		List<Product> resultList = new ArrayList<>();
		for (Product prod : products) {
			if (prod.getProduct_line().equalsIgnoreCase(Key)) {
				String resSym = StringUtils.difference(prod.getProduct_generic(), prod.getProduct_line());
				LOGGER.info("filterByLine:: Product_Generic: " + prod.getProduct_generic() + "\n" + "Product_Line: "
						+ prod.getProduct_line() + "\n" + "Result:" + resSym);
				prod.setProdDiff(resSym);
				resultList.add(prod);
			}
		}
		return resultList;
	}

	private List<Product> filterByBrand(String Key, List<Product> listProd) {
		LOGGER.info("ProductController: filterByBrand()");
		List<Product> resultList = new ArrayList<>();
		for (Product prod : listProd) {
			if (prod.getProduct_brand().equalsIgnoreCase(Key)) {
				String resSym = StringUtils.difference(prod.getProduct_generic(), prod.getProduct_line());
				LOGGER.info("filterByLine:: Product_Generic: " + prod.getProduct_generic() + "\n" + "Product_Line: "
						+ prod.getProduct_line() + "\n" + "Result:" + resSym);
				prod.setProdDiff(resSym);
				resultList.add(prod);
			}
		}
		return resultList;
	}

	private List<Product> filterByPrice(String Key, List<Product> listProd) {
		LOGGER.info("ProductController: filterByPrice()");
		List<Product> resultList = new ArrayList<>();
		for (Product prod : listProd) {
			proPrice = new BigDecimal(String.valueOf(prod.getProduct_price()));
			proPriceReq = new BigDecimal(String.valueOf(Key));
			if (proPrice.equals(proPriceReq)) {
				String resSym = StringUtils.difference(prod.getProduct_generic(), prod.getProduct_line());
				LOGGER.info("filterByLine:: Product_Generic: " + prod.getProduct_generic() + "\n" + "Product_Line: "
						+ prod.getProduct_line() + "\n" + "Result:" + resSym);
				prod.setProdDiff(resSym);
				resultList.add(prod);
			}
		}
		return resultList;
	}

	private BigDecimal sumOfFilteredProd(String product_line, List<Product> filteredProd) {
		LOGGER.info("ProductController: sumOfFilteredProd()");
		List<BigDecimal> sumOfProdPrice = new ArrayList<>();
		for (Product prod : filteredProd) {
			if (prod.getProduct_line().equalsIgnoreCase(product_line)) {
				sumOfProdPrice.add(new BigDecimal(String.valueOf(prod.getProduct_price())));
			}
		}
		LOGGER.info("sumOfFilteredProd():: Sum of filtered products(filtered by product_line) - "
				+ sumOfProdPrice.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
		return sumOfProdPrice.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

	}

	@RequestMapping(value = "/getDif")
	public ResponseEntity<List<String>> getProductsDifference() {
		LOGGER.info("ProductController: getProductsDifference()");
		List<Product> products = prodList;
		List<String> res = new ArrayList<>();
		String resSym = "";
		String result = "";
		if (!prodList.isEmpty()) {
			for (Product prod : products) {
				resSym = StringUtils.difference(prod.getProduct_generic(), prod.getProduct_line());
				result = "Product_Generic: " + prod.getProduct_generic() + " and Product_Line: "
						+ prod.getProduct_line() + " and Result: " + resSym;
				res.add(result);
				LOGGER.info("getProductsDifference():: Product_Generic: " + prod.getProduct_generic() + "\n"
						+ "Product_Line: " + prod.getProduct_line() + "\n" + "Result: " + resSym);
				prod.setProdDiff(resSym);
			}
		} else {
			LOGGER.info("getProductsDifference():: No products available");
		}
		return new ResponseEntity<List<String>>(res, HttpStatus.OK);
	}
}
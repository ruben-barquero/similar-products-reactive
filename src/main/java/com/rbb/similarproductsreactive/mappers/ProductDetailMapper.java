package com.rbb.similarproductsreactive.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.rbb.similarproducts.openapi.model.ProductDetail;
import com.rbb.similarproductsreactive.models.ProductDetailEntity;

@Mapper
public interface ProductDetailMapper {

	final ProductDetailMapper INSTANCE = Mappers.getMapper(ProductDetailMapper.class);
	 
    ProductDetail productDetailEntityToProductDetail(ProductDetailEntity productDetailEntity);
	
}

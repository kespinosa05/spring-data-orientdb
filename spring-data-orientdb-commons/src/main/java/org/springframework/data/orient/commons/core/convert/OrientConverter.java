package org.springframework.data.orient.commons.core.convert;

import org.springframework.data.convert.EntityConverter;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.orient.commons.core.mapping.OrientPersistentEntity;
import org.springframework.data.orient.commons.core.mapping.OrientPersistentProperty;

public interface OrientConverter
extends EntityConverter<OrientPersistentEntity<?>, OrientPersistentProperty, Object, Object>, 
EntityReader<Object, Object> {
	
}
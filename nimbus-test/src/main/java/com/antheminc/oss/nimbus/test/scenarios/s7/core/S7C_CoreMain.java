/**
 *  Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.test.scenarios.s7.core;

import java.time.LocalDate;

import com.antheminc.oss.nimbus.domain.defn.Domain;
import com.antheminc.oss.nimbus.domain.defn.Domain.ListenerType;
import com.antheminc.oss.nimbus.domain.defn.Repo;
import com.antheminc.oss.nimbus.domain.defn.Repo.Cache;
import com.antheminc.oss.nimbus.domain.defn.Repo.Database;
import com.antheminc.oss.nimbus.entity.AbstractEntity.IdLong;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Swetha Vemuri
 * @author Rakesh Patel
 *
 */
@Domain(value="s7c_main", includeListeners = { ListenerType.persistence })
@Repo(alias = "s7c_main", value = Database.rep_mongodb, cache = Cache.rep_device)
@Getter @Setter @ToString
public class S7C_CoreMain extends IdLong {
	
	private static final long serialVersionUID = 1L;
	private String attr1;
	private String attr1_clone;
	private LocalDate date1;
	
	
	@Domain(value="s7c_corestatic", includeListeners = { ListenerType.persistence })
	@Repo(alias = "s7c_corestatic", value = Database.rep_mongodb, cache = Cache.rep_device)
	@Getter @Setter
	public static class S7C_CoreStatic extends IdLong {
		
		private static final long serialVersionUID = 1L;
		private String staticAttr;
	}
}

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
package com.antheminc.oss.nimbus.test.scenarios.s10.view;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.antheminc.oss.nimbus.domain.defn.Domain;
import com.antheminc.oss.nimbus.domain.defn.Domain.ListenerType;
import com.antheminc.oss.nimbus.domain.defn.Execution.Config;
import com.antheminc.oss.nimbus.domain.defn.MapsTo;
import com.antheminc.oss.nimbus.domain.defn.MapsTo.Path;
import com.antheminc.oss.nimbus.domain.defn.MapsTo.Type;
import com.antheminc.oss.nimbus.domain.defn.Model;
import com.antheminc.oss.nimbus.domain.defn.Model.Param.Values;
import com.antheminc.oss.nimbus.domain.defn.Repo;
import com.antheminc.oss.nimbus.domain.defn.Repo.Cache;
import com.antheminc.oss.nimbus.domain.defn.Repo.Database;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Button;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.ComboBox;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Form;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Page;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Section;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.TextBox;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Tile;
import com.antheminc.oss.nimbus.domain.defn.extension.ActivateConditional;
import com.antheminc.oss.nimbus.domain.defn.extension.ActivateConditionals;
import com.antheminc.oss.nimbus.domain.defn.extension.ConfigConditional;
import com.antheminc.oss.nimbus.test.scenarios.s9.core.S9C_CoreMain;
import com.antheminc.oss.nimbus.test.scenarios.s9.core.SampleValues.SV1;
import com.antheminc.oss.nimbus.test.scenarios.s9.core.SampleValues.SV2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Andrew Jo
 * @author Tony Lopez
 *
 */
@Domain(value = "s10v_main", includeListeners = { ListenerType.websocket })
@Type(S9C_CoreMain.class)
@Repo(value=Database.rep_none, cache=Cache.rep_device)
@Getter @Setter @ToString
public class S10V_ViewMain {
	
	@Page
	private VPMain vp;
	
	@Model @Getter @Setter
	public static class VPMain {
	
		@Tile
		private VTMain vt;
	}
	
	@Model @Getter @Setter
	public static class VTMain {
		
		@Section
		private VSMain vs;
	}
	
	@Model @Getter @Setter
	public static class VSMain {
		
		@Form
		private VFMain vf;
	}
	
	@MapsTo.Type(S9C_CoreMain.class)
	@Model @Getter @Setter
	public static class VFMain {
		
		//@Label("v1")
		@ComboBox(postEventOnChange = true)
		@Values(value = SV1.class)
		@NotNull
		@ConfigConditional(when = "state != null", config = {
			@Config(url = "<!#this!>/../../_process?fn=_setByRule&rule=testrule")
//			@Config(url = "<!#this!>/../v2/_process?fn=_set&value=code2")
        })
        @ActivateConditional(when = "state == 'code1'", targetPath = { "/../v2" })
		@ActivateConditionals({
			@ActivateConditional(when="state == 'code1'", targetPath="/../v2"),
			@ActivateConditional(when="state == '2018'", targetPath="/../v3")
		})
		@Path
		private String v1;
		
		//@Label("Cause of Death")
        @ComboBox(postEventOnChange = true)
		@Values(value = SV2.class)
		@NotNull
		@Path(linked = false)
        private String v2;
        
		//@Label("Cause of Death")
        @ComboBox(postEventOnChange = true)
		@Values(value = SV2.class)
		@NotNull
		@Path(linked = false)
        private String v3;
        
        // Default valid config without any SpEL
        @Button
//      @Config(url = "<!#this!>/../foodToServe/_process?fn=_set&url=/s10v_main:1/vp/vt/vs/vf/favoriteFood1")
        @Config(url = "<!#this!>/../favoriteFood1/_process?fn=_set&value=apple")
        private String btnSetFoodToServeNoSpel1;
        
        // Default invalid path config without any SpEL
        @Button
//      @Config(url = "<!#this!>/../foodToServe/_process?fn=_set&url=/s10v_main:1/vp/vt/vs/vf/favoriteFood1")
        @Config(url = "<!#this!>/../favoriteFood3/_process?fn=_set&value=apple")
        private String btnSetFoodToServeNoSpel3;
        
        // Config with SpEL expression paths
        @Button
//        @Config(url = "<!#this!>/../foodToServe/_process?fn=_set&url=/s10v_main:1/vp/vt/vs/vf/favoriteFood1")
        @Config(url = "<!#this!>/../favoriteFood#{findStateByPath('/../foodPreference')}/_process?fn=_set&value=apple")
        private String btnSetFoodToServe;
        
        private Integer foodPreference;
        
        private String foodToServe;
        
        @TextBox
        private String favoriteFood1;
        
        @TextBox
        private String favoriteFood2;
        
	}
}

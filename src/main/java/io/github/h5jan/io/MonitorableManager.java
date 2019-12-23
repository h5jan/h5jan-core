/*-
 * 
 * Copyright 2019 Halliburton Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.h5jan.io;

import org.eclipse.january.IMonitor;

public interface MonitorableManager {

	  
	  /**
	   * Used to check a monitor's cancelled status.
	   * @param mon if null the job will not be cancelled.
	   * @return true if monitor is non-null and cancelled.
	   */
	  default boolean isCancelled(IMonitor mon) {
		  return mon!=null ? mon.isCancelled() : false;
	  }
	  
	  /**
	   * Used to increment a monitor's worked amount or do nothing if there is no monitor.
	   * @param mon if null the job will not be incremented.
	   */
	  default void worked(IMonitor mon) {
		  if (mon!=null) 
			  mon.worked(1);
	  }

}

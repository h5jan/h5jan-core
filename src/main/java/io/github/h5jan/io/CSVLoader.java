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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.AbstractDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.StringDataset;

import io.github.h5jan.core.DataFrame;


/**
 * Read wells in 2017 SEG ML format
 */
class CsvLoader extends AbstractStreamLoader implements IStreamLoader {

	public static final String DEPTH_COL_NAME = "DEPTH";

	private int limit = Integer.MAX_VALUE;

	@Override
	public DataFrame load(InputStream stream, Configuration configuration, IMonitor mon) throws IOException, DatasetException {

		final Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader()
													.parse( new InputStreamReader( stream, "UTF-8" ) );

		Map<String, List<Object>> logsMap = readWellSamples(configuration.getFilterName(), records, mon );
		List<Dataset> logs = logsMap.entrySet().stream().map( CsvLoader::exactLogSamplesToData ).collect( Collectors.toList() );

		String name = configuration.getFileName();
		final String frameName = name.substring( 0, name.indexOf( '.' ) );

		return new DataFrame(frameName, AbstractDataset.FLOAT32, logs);
	}

	private Map<String, List<Object>> readWellSamples( String logNameFilter, Iterable<CSVRecord> records,
			                                           final IMonitor mon ) throws IOException {


		int count = 0;
		Map<String, List<Object>> result = new LinkedHashMap<>(); // Order must be kept
		RECORD_LOOP: for (CSVRecord csvRecord : records) {

			count++;
			if (count>getLimit()) { // Can use limit to truncate data read
				break RECORD_LOOP;
			}

			for (Map.Entry<String, String> col : csvRecord.toMap().entrySet()) {
				final String logName = col.getKey().trim();

				if (logNameFilter != null && !Pattern.matches( logNameFilter, logName )) continue;
				final String logValue = col.getValue().trim();

				if (isCancelled( mon )) {
					throw new IOException( "The load job was cancelled" );
				}
				List<Object> wellSamples = result.get( logName );
				if (wellSamples==null) {
					wellSamples = new ArrayList<>();
					result.put( logName, wellSamples );
				}
				wellSamples.add(parse(logValue));
			}
		}
		return result;
	}

	private static final Pattern intPattern = Pattern.compile("\\d+");
	private static final Pattern  floatPattern = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)|(0\\.)");
	
	// Parses string to number if it can otherwise returns String
	private Object parse(String logValue) {
		
		if (logValue==null || "".equals(logValue.trim())) {
			return Float.NaN; // TODO This puts "NaN" in strings that have to be cleaned later
		}
		if (intPattern.matcher(logValue).matches()) {
			return Integer.parseInt(logValue);
		} else if (floatPattern.matcher(logValue).matches()) {
			return Float.parseFloat(logValue);
		}
		return logValue;
	}

	/**
	 * Used to process a Log when the SamplingMode is EXACT_DEPTH
	 * 
	 * @param log
	 * @return non-null instance of WellData
	 */
	private static Dataset exactLogSamplesToData( Map.Entry<String, List<Object>> log ) {
		final String key = log.getKey();

		final List<Object> logSamples = log.getValue();
		Dataset ret = DatasetFactory.createFromObject( logSamples );
		ret.setName(key);
		
		// We have to clean NaNs from string datasets. This does mean that the String
		// value "NaN" would get replaced with null which is not ideal however it also
		// means that you don't have to parse data twice, once to find type, once to allocate values.
		// In the context of this loader, this HACK is warranted. NaN's are not allowed in 
		// StringDatasets therefore.
		if (ret instanceof StringDataset) {
			String[] buf = (String[])ret.getBuffer();
			for (int i = 0; i < buf.length; i++) {
				if (String.valueOf(Float.NaN).equals(buf[i])) {
					buf[i] = "";
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Calculate the difference in depths.
	 * 
	 * @param depths
	 * @return a non-null instance of Dataset
	 */
	public static Dataset calculateDifferences( List<Number> depths ) {
		Dataset dset = Maths.centralDifference( DatasetFactory.createFromList( depths ), 0 );
		double variance = dset.variance();
		double minVariance = Double
				.parseDouble( System.getProperty( "com.halliburton.lgc.gemasis.awc.minimumDepthVariance", "0.01" ) );
		if (variance > minVariance) {
			throw new RuntimeException( "The depth intervals vary by more than " + minVariance + "m "
					+ "\nThe variance in depth is " + variance + "m" );
		}
		return dset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}


}
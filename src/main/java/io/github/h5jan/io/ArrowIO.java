package io.github.h5jan.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float4Vector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.SmallIntVector;
import org.apache.arrow.vector.VarBinaryVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.dictionary.DictionaryProvider;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowFileWriter;
import org.apache.arrow.vector.ipc.SeekableReadChannel;
import org.apache.arrow.vector.ipc.message.ArrowBlock;
import org.apache.arrow.vector.types.FloatingPointPrecision;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyDataset;

import com.google.common.collect.ImmutableList;

import io.github.h5jan.core.DataFrame;


/**
 * @see https://github.com/animeshtrivedi/blog/blob/master/post/2017-12-26-arrow.md
 * 
 * @author Matthew Gerring
 *
 */
class ArrowIO {
	
	static {
		// See https://issues.apache.org/jira/browse/ARROW-5412
		System.setProperty( "io.netty.tryReflectionSetAccessible","true"); 
	}
	
	private int batchSize = 1000;

	/**
	 * Read a prediction from proto
	 * @param path - to read
	 * @return LithologyPrediction
	 * @throws Exception - if something goes wrong.
	 */
	public DataFrame read(Path path) throws IOException, DatasetException {
		
		return read(new FileInputStream(path.toFile()));
	}
	
	/**
	 * Read a prediction from proto
	 * @param input - to read
	 * @return LithologyPrediction
	 * @throws Exception - if something goes wrong.
	 */
	public DataFrame read(FileInputStream input) throws IOException, DatasetException {
		return read(input.getChannel());
	}
	
	/**
	 * Read a prediction from proto
	 * @param input - to read. This channel can be from hadoop
	 * @return LithologyPrediction
	 * @throws Exception - if something goes wrong.
	 */
	public DataFrame read(SeekableByteChannel input) throws IOException, DatasetException {

		SeekableReadChannel seekableReadChannel = new SeekableReadChannel(input);
		try (ArrowFileReader arrowFileReader = new ArrowFileReader(seekableReadChannel, new RootAllocator(Integer.MAX_VALUE))) {
			VectorSchemaRoot root  = arrowFileReader.getVectorSchemaRoot(); // get root 
			
			// Load all into memory... (this might not be doable)
			Map<String, Object> columns = new LinkedHashMap<>(); 
			// Make the primitive arrays
			for (FieldVector fv : root.getFieldVectors()) {
				columns.put(fv.getField().getName(), getPrimitiveArray(fv.getField()));
			}
			
			int offset = 0;
			List<ArrowBlock> blocks = arrowFileReader.getRecordBlocks();
			for (ArrowBlock arrowBlock : blocks) {
				arrowFileReader.loadRecordBatch(arrowBlock);
	
				List<FieldVector> vectors = root.getFieldVectors();
				for (FieldVector fv : vectors) {
					if (!columns.containsKey(fv.getName())) columns.put(fv.getName(), new ArrayList<>(89));
					read(fv, columns.get(fv.getName()), offset); // Copy data into array
				}
				offset+=getBatchSize();
			}
			
			List<Dataset> data = convert(root.getSchema().getFields(), columns);
			return new DataFrame("Lithology Prediction", Dataset.FLOAT32, data);
		}
	}

	private Object getPrimitiveArray(Field field) throws DatasetException {
		int size = Integer.parseInt(field.getMetadata().get("size"));
		int dtype = Integer.parseInt(field.getMetadata().get("dtype"));
		switch(dtype) {
		
		// TODO Others!
		case Dataset.INT16:
			return new short[size];
		case Dataset.INT32:
			return new int[size];
		case Dataset.INT64:
			return new long[size];
		case Dataset.FLOAT32:
			return new float[size];
		case Dataset.FLOAT64:
			return new double[size];
		case Dataset.STRING:
			return new String[size];
		}
		throw new DatasetException("Cannot read primitive information from data frame metadata!");
	}

	private List<Dataset> convert(List<Field> fields, Map<String, Object> columns) {
		List<Dataset> ret = new ArrayList<Dataset>();
		for (Field field : fields) {
			Object values = columns.get(field.getName());
			int[] shape = fromString(field.getMetadata().get("shape"));
			Dataset set = DatasetFactory.createFromObject(values);
			if (shape.length>1 ) set.setShape(shape); // Can be an expensive operation!
			set.setName(field.getName());
			ret.add(set);
		}
		return ret;
	}

	private static int[] fromString(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split(", ");
		int result[] = new int[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(strings[i]);
		}
		return result;
	}
	
	private void read(FieldVector field, Object arr, int offset) throws UnsupportedEncodingException, DatasetException {
		for (int i = 0; i <  field.getValueCount(); i++) {
			read(field, arr, offset, i);
		}
	}

	private void read(FieldVector fv, Object arr, int offset, int i) throws UnsupportedEncodingException, DatasetException {
		int dtype = Integer.parseInt(fv.getField().getMetadata().get("dtype"));
		switch(dtype) {
		
		// TODO Others!
		case Dataset.INT16:
			Array.setShort(arr, offset+i, ((SmallIntVector)fv).get(i));
			return;
		case Dataset.INT32:
			Array.setInt(arr, offset+i, ((IntVector)fv).get(i));
			return;
		case Dataset.INT64:
			Array.setLong(arr, offset+i, ((BigIntVector)fv).get(i));
			return;
		case Dataset.FLOAT32:
			Array.setFloat(arr, offset+i, ((Float4Vector)fv).get(i));
			return;
		case Dataset.FLOAT64:
			Array.setDouble(arr, offset+i, ((Float8Vector)fv).get(i));
			return;
		case Dataset.STRING:
			VarBinaryVector binVect = (VarBinaryVector)fv;
			String value = new String(binVect.get(i), "UTF-8");
			Array.set(arr, offset+i, value);
			return;
		}
		throw new DatasetException("Cannot read primitive information from data frame metadata!");
	}

	/**
	 * Create a schema for the arrow file which holds a lithology prediction.
	 * 
	 * @return
	 * @throws DatasetException 
	 */
	private Schema createSchema(DataFrame frame) throws DatasetException {
		
		ImmutableList.Builder<Field> childrenBuilder = ImmutableList.builder();
		
		for (String columnName : frame.getColumnNames()) {
			ILazyDataset lz = frame.get(columnName);
			Field field = new Field(columnName, getFieldType(lz, columnName, frame), null);
			childrenBuilder.add(field);
		}

		return new Schema(childrenBuilder.build(), null);
	}
	
	private FieldType getFieldType(ILazyDataset lz, String columnName, DataFrame frame) throws DatasetException {
		int dtype = DTypeUtils.getDType(frame.get(columnName));
		int dtypeDef = frame.getDtype();
		return getFieldType(lz, dtype, dtypeDef);
	}

	private FieldType getFieldType(ILazyDataset lz, int dtype, int dtypeDef) {
		FieldType  type = getFieldType(lz, dtype);
		if (type == null) type = getFieldType(lz, dtypeDef);
		if (type ==null) type = FieldType.nullable(new ArrowType.FloatingPoint(FloatingPointPrecision.SINGLE));
		return type;
	}

	private FieldType getFieldType(ILazyDataset lz, int dtype) {
		
		ArrowType type = null;
		switch(dtype) {
		
		// TODO Others!
		case Dataset.INT16:
			type = new ArrowType.Int(16, true);
			break;
		case Dataset.INT32:
			type = new ArrowType.Int(32, true);
			break;
		case Dataset.INT64:
			type = new ArrowType.Int(64, true);
			break;
		case Dataset.FLOAT32:
			type = new ArrowType.FloatingPoint(FloatingPointPrecision.SINGLE);
			break;
		case Dataset.FLOAT64:
			type = new ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE);
			break;
		case Dataset.STRING:
			type = new ArrowType.Binary();
			break;
		}
		
		if (type==null) return null;
		return new FieldType(true, type, null, createMetadata(lz));
	}

	private Map<String, String> createMetadata(ILazyDataset lz) {
		Map<String, String> meta = new HashMap<String, String>();
		meta.put("shape", Arrays.toString(lz.getShape()));
		meta.put("dtype", String.valueOf(DTypeUtils.getDType(lz)));
		meta.put("dname", String.valueOf(lz.getName()));
		meta.put("size", String.valueOf(lz.getSize()));
		return meta;
	}

	/**
	 * Write frame to arrow.
	 * @param frame
	 * @param output e.g. FileOutputStream.getChannel()
	 * @throws IOException
	 * @throws DatasetException 
	 */
	public boolean write(DataFrame frame, FileOutputStream stream) throws IOException, DatasetException {
		return write(frame, stream.getChannel());
	}
	
	/**
	 * Write frame to arrow.
	 * @param frame
	 * @param output e.g. FileOutputStream.getChannel()
	 * @return true if written
	 * @throws IOException
	 * @throws DatasetException 
	 */
	public boolean write(DataFrame frame, WritableByteChannel output) throws IOException, DatasetException {
		
		Schema schema = createSchema(frame);
		VectorSchemaRoot root = VectorSchemaRoot.create(schema, new RootAllocator(Integer.MAX_VALUE));
		
		ArrowFileWriter writer = new ArrowFileWriter(root, new DictionaryProvider.MapDictionaryProvider(), output);
		try {
			writer.start();
			
			Map<String, Object> raw = frame.raw(); // Load raw data arrays. This allows nD writing to work.
			
			final int entries = frame.get(0).getSize(); // Total size of the buffer
			for (int i = 0; i < entries; i++) {
				int toProcessItems = Math.min(this.batchSize, entries - i);
			    root.setRowCount(toProcessItems); // Batches

	            for (Field field : root.getSchema().getFields()) {
	                FieldVector vector = root.getVector(field.getName());
	                Object array = raw.get(field.getName());
	                switch (vector.getMinorType()) {
	                    case SMALLINT:
	                        writeFieldShort(vector, i, toProcessItems, array);
	                        break;
	                    case INT:
	                        writeFieldInt(vector, i, toProcessItems, array);
	                        break;
	                    case BIGINT:
	                        writeFieldLong(vector, i, toProcessItems, array);
	                        break;
	                    case FLOAT4:
	                        writeFieldFloat4(vector, i, toProcessItems, array);
	                        break;
	                    case FLOAT8:
	                        writeFieldFloat8(vector, i, toProcessItems, array);
	                        break;
	                    case VARBINARY:
	                    	writeFieldVarBinary(vector, i, toProcessItems, array);
	                        break;
	                    default:
	                        throw new DatasetException(" Not supported yet type: " + vector.getMinorType());
	                }
	            }
			    writer.writeBatch();
				i+=toProcessItems-1;
			}
		    
		} finally {
			writer.end();
			writer.close();
		}
		return true;
	}
	
    private void writeFieldShort(FieldVector fieldVector, int from, int items, Object array){
        SmallIntVector intVector = (SmallIntVector) fieldVector;
        intVector.setInitialCapacity(items);
        intVector.allocateNew();
        for(int i = 0; i < items; i++) {
        	Number number = (Number)Array.get(array, from+i);
            intVector.setSafe(i, number.shortValue());
        }
        // how many are set
        fieldVector.setValueCount(items);
    }

    private void writeFieldInt(FieldVector fieldVector, int from, int items, Object array){
        IntVector intVector = (IntVector) fieldVector;
        intVector.setInitialCapacity(items);
        intVector.allocateNew();
        for(int i = 0; i < items; i++){
           	Number number = (Number)Array.get(array, from+i);
            intVector.setSafe(i, number.intValue());
        }
        // how many are set
        fieldVector.setValueCount(items);
    }

    private void writeFieldLong(FieldVector fieldVector, int from, int items, Object array){
        BigIntVector bigIntVector = (BigIntVector) fieldVector;
        bigIntVector.setInitialCapacity(items);
        bigIntVector.allocateNew();
        for(int i = 0; i < items; i++){
           	Number number = (Number)Array.get(array, from+i);
        	bigIntVector.setSafe(i, number.longValue());
        }
        // how many are set
        bigIntVector.setValueCount(items);
    }

    private void writeFieldFloat4(FieldVector fieldVector, int from, int items, Object array){
        Float4Vector float4Vector  = (Float4Vector ) fieldVector;
        float4Vector.setInitialCapacity(items);
        float4Vector.allocateNew();
        for(int i = 0; i < items; i++){
           	Number number = (Number)Array.get(array, from+i);
            float4Vector.setSafe(i, number.floatValue());
        }
        // how many are set
        float4Vector.setValueCount(items);
    }

    private void writeFieldFloat8(FieldVector fieldVector, int from, int items, Object array){
        Float8Vector float8Vector  = (Float8Vector ) fieldVector;
        float8Vector.setInitialCapacity(items);
        float8Vector.allocateNew();
        for(int i = 0; i < items; i++){
           	Number number = (Number)Array.get(array, from+i);
            float8Vector.setSafe(i, number.doubleValue());
        }
        // how many are set
        float8Vector.setValueCount(items);
    }
    
    private void writeFieldVarBinary(FieldVector fieldVector, int from, int items, Object array) throws UnsupportedEncodingException{
        VarBinaryVector varBinaryVector = (VarBinaryVector) fieldVector;
        varBinaryVector.setInitialCapacity(items);
        varBinaryVector.allocateNew();
        for(int i = 0; i < items; i++){
        	
           	String value = Array.get(array, from+i).toString();
           	byte[] data  = value.getBytes("UTF-8");
        	varBinaryVector.setIndexDefined(i);
        	varBinaryVector.setValueLengthSafe(i, data.length);
        	varBinaryVector.setSafe(i, data);
            
        }
        // how many are set
        varBinaryVector.setValueCount(items);
    }
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}

package org.springframework.data.cassandra.convert;

import com.datastax.driver.core.*;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Helpful class to read a column's value from a row, with possible type conversion.
 *
 * @author Matthew T. Adams
 * @author Antoine Toulme
 * @author Mark Paluch
 */
public class ColumnReader {

	private final Row row;
	private final ColumnDefinitions columns;
	private final CodecRegistry codecRegistry;

	public ColumnReader(Row row) {

		this.row = row;
		this.columns = row.getColumnDefinitions();
		this.codecRegistry = CodecRegistry.DEFAULT_INSTANCE;
	}

	/**
	 * Returns the row's column value.
	 */
	public Object get(String columnName) {
		return get(getColumnIndex(columnName));
	}

	/**
	 * Read data from Column at the given {@code index}.
	 *
	 * @param columnIndex {@link Integer#TYPE index} of the Column.
	 * @return the value of the Column in at index in the Row, or {@literal null} if the Column contains no value.
	 */
	public Object get(int columnIndex) {
		if (row.isNull(columnIndex)) {
			return null;
		}

		final DataType type = columns.getType(columnIndex);

		if (type.isCollection()) {
			return getCollection(columnIndex, type);
		}

		if (DataType.Name.TUPLE.equals(type.getName())) {
			return row.getTupleValue(columnIndex);
		}

		if (DataType.Name.UDT.equals(type.getName())) {
			return row.getUDTValue(columnIndex);
		}

		return row.getObject(columnIndex);
	}

	/**
	 * Returns the row's column value as an instance of the given type.
	 *
	 * @throws ClassCastException if the value cannot be converted to the requested type.
	 */
	public <T> T get(String columnName, Class<T> requestedType) {
		return get(getColumnIndex(columnName), requestedType);
	}

	/**
	 * Returns the row's column value as an instance of the given type.
	 *
	 * @throws ClassCastException if the value cannot be converted to the requested type.
	 */
	public <T> T get(int columnIndex, Class<T> requestedType) {
		final Object value = get(columnIndex);

		return requestedType.cast(value);
	}

	private Object getCollection(int index, DataType type) {
		final List<DataType> collectionTypes = type.getTypeArguments();

		// List/Set
		if (collectionTypes.size() == 1) {

			final DataType valueType = collectionTypes.get(0);

			final TypeCodec<Object> typeCodec = codecRegistry.codecFor(valueType);

			if (type.equals(DataType.list(valueType))) {
				return row.getList(index, typeCodec.getJavaType().getRawType());
			}

			if (type.equals(DataType.set(valueType))) {
				return row.getSet(index, typeCodec.getJavaType().getRawType());
			}
		}

		// Map
		if (type.getName() == DataType.Name.MAP) {
			return row.getObject(index);
		}

		throw new IllegalStateException("Unknown Collection type encountered; valid collections are List, Set and Map.");
	}

	private int getColumnIndex(String columnName) {
		final int index = columns.getIndexOf(columnName);

		Assert.isTrue(index > -1, String.format("Column [%s] does not exist in table", columnName));

		return index;
	}

	public Row getRow() {
		return row;
	}
}

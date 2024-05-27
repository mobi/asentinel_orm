package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * These tests would fail on versions lower than 1.58.31
 *  
 * @author Razvan Popian
 */
public class EntityBuilderSortedSetTestCase {
	
	private final Node<EntityDescriptor> parkingLotNode = new DefaultEntityDescriptorTreeRepository().getEntityDescriptorTree(ParkingLot.class);
	private final SimpleEntityDescriptor parkingLot = (SimpleEntityDescriptor) parkingLotNode.getValue();
	private final SimpleEntityDescriptor car = (SimpleEntityDescriptor) parkingLotNode.getChildren().get(0).getValue();
	private final SimpleEntityDescriptor type = (SimpleEntityDescriptor) parkingLotNode.getChildren().get(0).getChildren().get(0).getValue();
	private final EntityBuilder<ParkingLot> eb = new EntityBuilder<>(parkingLotNode);
	
	private final ResultSet rs = mock(ResultSet.class);

	@Before
	public void setup() throws SQLException {
		when(rs.getInt(parkingLot.getTableAlias() + parkingLot.getColumnAliasSeparator() + "id"))
			.thenReturn(1);
	
		when(rs.getInt(car.getTableAlias() + car.getColumnAliasSeparator() + "id"))
			.thenReturn(20)
			.thenReturn(10)
			.thenReturn(30);
	
		when(rs.getInt(type.getTableAlias() + type.getColumnAliasSeparator() + "id"))
			.thenReturn(200)
			.thenReturn(100)
			.thenReturn(300);
	}
	
	private void processRows() throws SQLException {
		// simulate the processing of 3 rows, 
		// but a single ParkingLot entity will be created
		eb.processRow(rs);
		eb.processRow(rs);
		eb.processRow(rs);
	}
	
	
	@Test
	public void getEntity() throws SQLException {
		processRows();
		
		ParkingLot pl = eb.getEntity();
		
		assertNotNull(pl);
		assertEquals(3, pl.cars.size());
		assertEquals(10, pl.cars.first().id);
		assertEquals(30, pl.cars.last().id);
	}
	
	
	@Test
	public void getEntities() throws SQLException {
		processRows();
		
		List<ParkingLot> pls = eb.getEntityList();
		
		assertEquals(1, pls.size());
		assertEquals(3, pls.get(0).cars.size());
		assertEquals(10, pls.get(0).cars.first().id);
		assertEquals(30, pls.get(0).cars.last().id);
	}
	

	@Test
	public void getEntityMap() throws SQLException {
		processRows();
		
		Map<Object, ParkingLot> pls = eb.getEntityMap();
		
		assertEquals(1, pls.size());
		assertEquals(3, pls.get(1).cars.size());
		assertEquals(10, pls.get(1).cars.first().id);
		assertEquals(30, pls.get(1).cars.last().id);
	}
	
	@Table("ParkingLot")
	private static class ParkingLot {
		
		@PkColumn("id")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		SortedSet<Car> cars;

		@Override
		public String toString() {
			return "ParkingLot [id=" + id + ", cars=" + cars + "]";
		}
	}
	
	@Table("Car")
	private static class Car implements Comparable<Car> {
		@PkColumn("id")
		int id;
		
		@Child
		Type type;
		
		@Override
		public int hashCode() {
			return type.id;
		}

		@Override
		public int compareTo(Car o) {
			return Integer.compare(type.id, o.type.id);
		}


		@Override
		public boolean equals(Object obj) {
			Car other = (Car) obj;
			return type.id == other.type.id;
		}
		
		@Override
		public String toString() {
			return "Car [id=" + id + ", type=" + type + "]";
		}

	}

	
	@Table("Type")
	private static class Type {
		@PkColumn("id")
		int id;

		@Override
		public String toString() {
			return "Type [id=" + id + "]";
		}
	}

	
}

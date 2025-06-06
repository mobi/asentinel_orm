# Introduction 

**asentinel-orm** is a light weight ORM tool written on top of Spring JDBC and `JdbcTemplate`. Initially, it was developed as an enhancement for the Asentinel TEM project whose data access layer was based on `JdbcTemplate`. Asentinel TEM is called [Tangoe Telecom](https://www.tangoe.com/) today and is a telecom expense management application. If you are using `JdbcTemplate` in your project but you feel you could do better than just using `RowMapper` for mapping database resultsets or you want to stop writing manually inserts and updates, this library might be suitable for you.  
One of the Tangoe Telecom project features is to allow users to define their own custom fields. Consequently, one of the design goals of this ORM was to make it easy for the programmer to work with dynamic schemas and custom defined columns/fields.


# Key features

- simple configuration on top of `JdbcTemplate`
- easy to integrate in any `JdbcTemplate` project
- easy fallback to plain SQL statements and `JdbcTemplate`
- all SQL queries are executed using `JdbcTemplate` so they participate in Spring managed transactions
- simple central interface for working with the library - `OrmOperations` 
- automatically generates SQL statements (select, insert, update) based on entity classes annotations
- `SqlBuilder` class for writing SQL statements
- supports paginated select SQL statements out of the box
- supports lazy loading entities (entity proxies)
- supports dynamic schemas - fields can be added to tables at runtime and the tool is able to map them without any code changes. SQL statements (select, insert, update) are generated so that they include the dynamic columns.
- supports custom conversion from java types to database types and viceversa by leveraging the Spring ConversionService (since 1.71.0). 

# Example

Please see [asentinel-orm-demo](https://github.com/jMediaConverter/asentinel-orm-demo) for a working example. Many of the code snippets below are extracted from that project.

# Supported databases

The database specifics are abstracted behind an interface called `JdbcFlavor`. Currently out of the box this interface has implementations for the following databases.

- H2
- Postgres
- Oracle

It should be reasonably easy to add specific `JdbcFlavor` implementations for other databases. It's likely that we will add other out of the box implementations in the future.

# Add the library to your Maven project
```
<dependency>
    <groupId>com.asentinel.common</groupId>
    <artifactId>asentinel-common</artifactId>
    <version>1.71.0</version>
</dependency>
```

# Configuration for Spring application context

Here is how to configure the asentinel-orm to access an H2 database. The last bean created is the `OrmOperations` which is
the interface you will use to perform all the ORM actions.

```
@Bean
public DataSource dataSource() {
	return new SingleConnectionDataSource("jdbc:h2:mem:testdb", "sa", "", false);
}

@Bean
public JdbcFlavor jdbcFlavor() {
    return new H2JdbcFlavor();
}

@Bean
public JdbcOperations jdbcOperations(DataSource dataSource, JdbcFlavor jdbcFlavor) {
	return new JdbcTemplate(dataSource) {
		
		@Override
		protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
			return new CustomArgumentPreparedStatementSetter(jdbcFlavor, args);
		}
	};
}

@Bean
public SqlQuery sqlQuery(JdbcFlavor jdbcFlavor, JdbcOperations jdbcOps) {
    return new SqlQueryTemplate(jdbcFlavor, jdbcOps);
}

@Bean
public SqlFactory sqlFactory(JdbcFlavor jdbcFlavor) {
    return new DefaultSqlFactory(jdbcFlavor);
}

@Bean
public DefaultEntityDescriptorTreeRepository entityDescriptorTreeRepository(SqlBuilderFactory sqlBuilderFactory) {
    DefaultEntityDescriptorTreeRepository treeRepository = new DefaultEntityDescriptorTreeRepository();
    treeRepository.setSqlBuilderFactory(sqlBuilderFactory);
    return treeRepository;
}

@Bean
public DefaultSqlBuilderFactory sqlBuilderFactory(@Lazy EntityDescriptorTreeRepository entityDescriptorTreeRepository,
                                                  SqlFactory sqlFactory, SqlQuery sqlQuery) {
    DefaultSqlBuilderFactory sqlBuilderFactory = new DefaultSqlBuilderFactory(sqlFactory, sqlQuery);
    sqlBuilderFactory.setEntityDescriptorTreeRepository(entityDescriptorTreeRepository);
    return sqlBuilderFactory;
}

@Bean
public OrmOperations orm(JdbcFlavor jdbcFlavor, SqlQuery sqlQuery,
                         SqlBuilderFactory sqlBuilderFactory) {
    return new OrmTemplate(sqlBuilderFactory, new SimpleUpdater(jdbcFlavor, sqlQuery));
}
```

# Define two entities

We are defining here 2 entity classes `CarManufacturer` and `CarModel`. The relationship between them should be obvious,
a car manufacturer can have multiple car models. Getters and setters are omitted for brevity. Notice the annotations:
- `@Table` used to map the entity to a table in the database
- `@PkColumn` used to map the primary key column to the entity id
- `@Column` used to map a  regular column to a member in the entity class
- `@Child` used to define a relationship to another entity class

```
@Table("CarManufacturers")
public class CarManufacturer {
	
	public static final String COL_NAME = "name";

	@PkColumn("id")
	private int id;
	
	@Column(COL_NAME)
	private String name;
	
	@Child(parentRelationType = RelationType.MANY_TO_ONE, 
			fkName = CarModel.COL_CAR_MANUFACTURER, 
			fetchType = FetchType.LAZY)
	private List<CarModel> models = emptyList();

	// ORM constructor
	protected CarManufacturer() {
		
	}
	
	...
}

@Table("CarModels")
public class CarModel {
	
	public static final String COL_NAME = "name";
	public static final String COL_TYPE = "type";
	public static final String COL_CAR_MANUFACTURER = "CarManufacturer";
	
	@PkColumn("id")
	private int id;
	
	@Column(COL_NAME)
	private String name;
	
	@Column(COL_TYPE)
	private CarType type;
	
	@Child(fkName = COL_CAR_MANUFACTURER, fetchType = FetchType.LAZY)
	private CarManufacturer carManufacturer;
	
	// ORM constructor
	protected CarModel() {
		
	}
	...
}

public enum CarType {
	CAR, SUV, TRUCK
}
```

# SQL statements to create the tables in the database
```
CREATE TABLE CarManufacturers(ID INT auto_increment PRIMARY KEY,
	NAME VARCHAR(255));

CREATE TABLE CarModels(
	ID INT auto_increment PRIMARY KEY,
	CarManufacturer int,
	NAME VARCHAR(255),
	TYPE VARCHAR(15),
	foreign key (CarManufacturer) references CarManufacturers(id)
);
```

# Persist some entities in the database

```
@Autowired
private final OrmOperations orm;

private void persistSomeData() {
	logger.info("\n\npersist some data ...");
	CarManufacturer mazda = new CarManufacturer("Mazda");
	orm.update(mazda);
	CarModel mx5 = new CarModel("mx5", CarType.CAR, mazda);
	CarModel m3 = new CarModel("3", CarType.CAR, mazda);
	CarModel m6 = new CarModel("6", CarType.CAR, mazda);
	CarModel cx3 = new CarModel("cx3", CarType.SUV, mazda);
	orm.update(mx5, m3, m6, cx3);
	
	CarManufacturer honda = new CarManufacturer("Honda");
	orm.update(honda);
	
	CarModel accord = new CarModel("accord", CarType.CAR, honda);
	orm.update(accord);
	
	CarModel civic = new CarModel("civic", CarType.CAR, honda);
	CarModel crv = new CarModel("crv", CarType.SUV, honda);
	orm.update(civic, crv);
	
	CarManufacturer toyota = new CarManufacturer("Toyota");
	orm.update(toyota);
	orm.delete(CarManufacturer.class, toyota.getId());
}

```

# Load some `CarModel` entities using the `SqlBuilder` 

Notice that the `CarManufacturer` inside each `CarModel` is a proxy (it was declared lazy in the `CarModel` class)

```
private void loadSomeData() {
	logger.info("\n\nload some data ...");
	List<CarModel> models = orm.newSqlBuilder(CarModel.class)
			.select().orderBy().column(CarModel.COL_NAME)
			.exec();
	for (CarModel model: models) {
		logger.info(model);
		// check the manufacturer is lazily loaded, it is a proxy
		if (EntityUtils.isProxy(model.getCarManufacturer()) && EntityUtils.isLoadedProxy(model.getCarManufacturer())) {
			throw new RuntimeException("Something is wrong !");
		}
	}
}
```

# Load some `CarModel` entities using the `SqlBuilder` eager loading the `CarManufacturer` 

Notice the use of the `AutoEagerLoader` to eagerly load the `CarManufacturer` inside each `CarModel`.

```
private void loadSomeDataEagerly() {
	logger.info("\n\nload some data eagerly ...");
	List<CarModel> models = orm.newSqlBuilder(CarModel.class)
			.select(
				AutoEagerLoader.forPath(CarModel.class, CarManufacturer.class)
			)
			.orderBy().column(CarModel.COL_NAME)
			.exec();
	for (CarModel model: models) {
		logger.info(model);
		// check the manufacturer is eagerly loaded, it is NOT a proxy
		if (EntityUtils.isProxy(model.getCarManufacturer())) {
			throw new RuntimeException("Something is wrong !");
		}
	}
}
```

# Custom data types conversion

To support custom data types conversion a `ConversionService` has to be injected both in the `DefaultEntityDescriptorTreeRepository` and the `SimpleUpdater`. The appropiate converters have to be registered with the `ConversionService`. The methods that create the `DefaultEntityDescriptorTreeRepository` and `OrmOperations` shown in the configuration above will change like this:

```
	.....
	
    @Bean
    public DefaultEntityDescriptorTreeRepository entityDescriptorTreeRepository(SqlBuilderFactory sqlBuilderFactory,
    		@Qualifier("ormConversionService") ConversionService conversionService) {
        DefaultEntityDescriptorTreeRepository treeRepository = new DefaultEntityDescriptorTreeRepository();
        treeRepository.setSqlBuilderFactory(sqlBuilderFactory);
        treeRepository.setConversionService(conversionService);
        return treeRepository;
    }

    @Bean
    public OrmOperations orm(JdbcFlavor jdbcFlavor, SqlQuery sqlQuery,
                             SqlBuilderFactory sqlBuilderFactory,
                             @Qualifier("ormConversionService") ConversionService conversionService) {
    	SimpleUpdater updater = new SimpleUpdater(jdbcFlavor, sqlQuery);
    	updater.setConversionService(conversionService);
        return new OrmTemplate(sqlBuilderFactory, updater);
    }
    
    @Bean("ormConversionService")
    public ConversionService ormConversionService() {
    	GenericConversionService conversionService = new GenericConversionService();
    	conversionService.addConverter(new JsonToObjectConverter());
    	conversionService.addConverter(new ObjectToJsonConverter());
    	return conversionService;
    }
```

Note that we are also creating a `ConversionService` bean that is able to convert to/from JSONB. This example works for Postgres. See below the source code for the 2 JSONB converters:

```
	public class JsonToObjectConverter implements ConditionalGenericConverter {
		
		// Jackson mapper
		private static final ObjectMapper MAPPER = new ObjectMapper();

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			PGobject pgObj = (PGobject) source;
			try {
				return MAPPER.readValue(pgObj.getValue(), targetType.getType());
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException("Failed to convert from JSON.", e);
			}
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (sourceType.getType() != PGobject.class) {
				return false;
			}
			String sqlParamValue;
			if (targetType instanceof FieldIdTypeDescriptor) {
				// dynamic column case
				DynamicColumn column = (DynamicColumn) ((FieldIdTypeDescriptor) targetType).getFieldId(); 
				sqlParamValue = column.getSqlParameter().getTypeName(); 
			} else {
				// static column case
				Column column = targetType.getAnnotation(Column.class);
				if (column == null) {
					return false;
				}
				sqlParamValue = column.sqlParam().value();
			}
			return "jsonb".equals(sqlParamValue);
		}
		
	}
	
	public class ObjectToJsonConverter implements ConditionalGenericConverter {
	
		// Jackson mapper	
		private static final ObjectMapper MAPPER = new ObjectMapper();
		
		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			String s;
			try {
				s = MAPPER.writeValueAsString(source);
				PGobject pgo = new PGobject();
				pgo.setType("jsonb");
				pgo.setValue(s);
				return pgo;
			} catch (JsonProcessingException | SQLException e) {
				throw new IllegalArgumentException("Failed to convert to JSON.", e);
			}
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (!(targetType instanceof SqlParameterTypeDescriptor)) {
				return false;
			}
			
			SqlParameterTypeDescriptor typeDescriptor = (SqlParameterTypeDescriptor) targetType;
			return "jsonb".equals(typeDescriptor.getTypeName());
		}
		
	}}

```

Below is a simple domain class that includes the id and an employee member that is stored as JSONB in a Postgres database. Notice the `@Column` annotation on the employee member declares the column name and a `@SqlParam` annotation that has the database type name as the value. This annotation will trigger the `ConversionService` for the annotated field causing the 2 converters declared above to be used for reading and writing the field. 

```
@Table("EmployeeHolder")
public class EmployeeHolder {

    @PkColumn("Id")
    private int id;
    
    @Column(value = "EmployeeJson", sqlParam = @SqlParam("jsonb"))
    private Employee employee;
    
    // getters/setters omitted for brevity
    ....
    
    /** Class represented as JSONB in the database */
    public static class Employee {
		private String firstName;
		private String lastName;
			
	    // getters/setters omitted for brevity
	    ....
	}
}   
```
Another useful scenario is to convert a certain java type to a `spring-jdbc` `SqlParameterValue` so that we can specify the exact SQL type, precision etc. Here is a pair of converters that can convert between `java.time.Instant` and `java.sql.Timestamp`:

```
	public class InstantToTimestampConverter implements ConditionalGenericConverter {

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			Instant instant = (Instant) source;
			Timestamp t = Timestamp.from(instant);
			return new SqlParameterValue(Types.TIMESTAMP, t);
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (sourceType.getType() != Instant.class) {
				return false;
			}
			if (!(targetType instanceof SqlParameterTypeDescriptor)) {
				return false;
			}			
			SqlParameterTypeDescriptor typeDescriptor = (SqlParameterTypeDescriptor) targetType;
			return "timestamp".equals(typeDescriptor.getTypeName());
		}
		
		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

	}
	
	public class TimestampToInstantConverter implements Converter<Timestamp, Instant> {

		@Override
		public Instant convert(Timestamp source) {
			return source.toInstant();
		}
	}
```
Any field annotated with `@Column(value = "SomeColumnName", sqlParam = @SqlParam("timestamp"))` will trigger the above converters assuming they are registered with the ORM `ConversionService`.

# Further reading
- [Dzone: Runtime-Defined Columns With asentinel-orm](https://dzone.com/articles/runtime-defined-columns-with-asentinel-orm)

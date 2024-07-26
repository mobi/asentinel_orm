# Introduction 

The asentinel-orm is a light weight ORM tool written on top of Spring JDBC and `JdbcTemplate`. It was developed initially as an enhancement for a project whose data access layer was based on JdbcTemplate. If you are using JdbcTemplate in your project but you feel you could do better than just using `RowMapper` for mapping database resultsets or you want to stop writing manually inserts and updates, this library might be for you.


# Key features

- simple configuration on top of JdbcTemplate
- easy to integrate in any `JdbcTemplate` project
- simple central interface for working with the library - `OrmOperations` 
- automatically generates SQL statements (select, insert, update) based on entity classes annotations
- 'SqlBuilder' class for writing SQL statements
- supports paginated select SQL statements out of the box
- supports lazy loading entities (entity proxies)
- supports dynamic schemas - fields can be added to tables at runtime and the tool is able to map them without any code changes


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
    <version>1.70.0</version>
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
a car manufacturer can have multiple car models. Getters and setters are omitted for brevity.

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

# Load some 'CarModel' entities using the `SqlBuilder` 

Notice that the 'CarManufacturer' inside each `CarModel` is a proxy (it was declared lazy in the `CarModel` class)

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

# Load some 'CarModel' entities using the `SqlBuilder` 

Notice the use of the `AutoEagerLoader` to eagerly load the 'CarManufacturer' inside each `CarModel`.

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

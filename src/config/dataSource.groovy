x = new Properties()
x.putAll([maxActive:"8",
                   maxIdle:"2",
                   username:"@db_username@",
                   validationQuery:"select 1 from rights",
                   password:"@db_password@",
                   removeAbandoned:"true",
                   removeAbandonedTimeout:"300",
	      		  timeBetweenEvictionRunsMillis:"300000",
                   driverClassName:"org.firebirdsql.jdbc.FBDriver",
                   url:"jdbc:firebirdsql://@db_url@"])

org.apache.commons.dbcp.BasicDataSourceFactory.createDataSource(x)
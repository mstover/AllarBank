x = new Properties()
x.putAll([maxActive:"8",
                   maxIdle:"2",
                   validationQuery:"select 1 from rights",
                   username:"damuser",
                   password:"r!VAld0",
                   driverClassName:"org.firebirdsql.jdbc.FBDriver",
                   url:"jdbc:firebirdsql://localhost/var/firebird/lazerweb.fdb"])

org.apache.commons.dbcp.BasicDataSourceFactory.createDataSource(x)
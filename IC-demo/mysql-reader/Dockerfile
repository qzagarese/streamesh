FROM mysql:8.0.18
COPY my.cnf /etc/mysql
COPY init.sql /docker-entrypoint-initdb.d
CMD ["mysqld"]


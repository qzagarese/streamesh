package io.scicast.simpledbreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class DBReader {

    private static final String HEADER = "id,name,host_id,host_name,neighbourhood_group,neighbourhood," +
            "latitude,longitude,room_type,price,minimum_nights,number_of_reviews,last_review,reviews_per_month," +
            "calculated_host_listings_count,availability_365";

    @Autowired
    DataSource ds;

    public void execute() throws IOException {
        JdbcTemplate template = new JdbcTemplate(ds);
        SqlRowSet sqlRowSet = template.queryForRowSet("select * from properties");

        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/data.csv"));
        bw.write(HEADER);
        bw.newLine();
        while (sqlRowSet.next()) {
            String line = toLine(sqlRowSet);
            bw.write(line);
            bw.newLine();
        }
        bw.flush();
        bw.close();

    }

    private String toLine(SqlRowSet sqlRowSet) {
        StringBuffer buffer = new StringBuffer();
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (metaData.getColumnTypeName(i).equalsIgnoreCase("int")) {
                buffer.append(sqlRowSet.getInt(i) + "");
            } else {
                byte[] object = (byte[]) sqlRowSet.getObject(i);
                buffer.append(new String(object));
            }
            if (i < metaData.getColumnCount()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
//| id                             | int(11) | NO   | PRI | NULL    |       |
//| name                           | blob    | YES  |     | NULL    |       |
//| host_id                        | int(11) | YES  |     | NULL    |       |
//| host_name                      | blob    | YES  |     | NULL    |       |
//| neighbourhood_group            | blob    | YES  |     | NULL    |       |
//| neighbourhood                  | blob    | YES  |     | NULL    |       |
//| latitude                       | blob    | YES  |     | NULL    |       |
//| longitude                      | blob    | YES  |     | NULL    |       |
//| room_type                      | blob    | YES  |     | NULL    |       |
//| price                          | blob    | YES  |     | NULL    |       |
//| minimum_nights                 | blob    | YES  |     | NULL    |       |
//| number_of_reviews              | blob    | YES  |     | NULL    |       |
//| last_review                    | blob    | YES  |     | NULL    |       |
//| reviews_per_month              | blob    | YES  |     | NULL    |       |
//| calculated_host_listings_count | blob    | YES  |     | NULL    |       |
//| availability_365               | blo
    }

}

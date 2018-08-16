package org.neo4j.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.ImpermanentGraphDatabase;

/**
 * @author mh
 * @since 14.06.12
 */
public class Neo4jJdbcPerformanceTestRunner
{

    private static final int RUNS = 10;
    private static final int COUNT = 5000;
    private ImpermanentGraphDatabase gdb;

    public Neo4jJdbcPerformanceTestRunner( ImpermanentGraphDatabase gdb )
    {
        this.gdb = gdb;
        createData( gdb, COUNT );
    }

    private void createData( GraphDatabaseService gdb, int count )
    {
        final DynamicRelationshipType type = DynamicRelationshipType.withName( "RELATED_TO" );
        final Transaction tx = gdb.beginTx();
        try
        {
            final Node node = gdb.createNode();
            for ( int i = 0; i < count; i++ )
            {
                final Node n = gdb.createNode();
                node.createRelationshipTo( n, type );
            }
            tx.success();
        }
        finally
        {
            tx.close();
        }

    }

    private long execute( final Connection con ) throws SQLException
    {
        long time = System.currentTimeMillis();
        final ResultSet rs = con.createStatement().executeQuery( "start n=node(*) match p=n-[r]->m return n," +
                "ID(n) as id, r,m,p" );
        int count = 0;
        while ( rs.next() )
        {
            rs.getInt( "id" );
            count++;
        }
        return System.currentTimeMillis() - time;
    }

    public void executeMultiple( Connection con ) throws SQLException
    {
        long delta = 0;
        execute( con );
        System.out.println( "START" );
        for ( int i = 0; i < RUNS; i++ )
        {
            delta += execute( con );
        }
        System.out.println( "Query took " + delta / RUNS + " ms." );
    }

}

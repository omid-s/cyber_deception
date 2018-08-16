/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

public class Neo4jDatabaseLockingDataTest extends Neo4jJdbcTest
{

    public Neo4jDatabaseLockingDataTest( Mode mode ) throws SQLException
    {
        super( mode );
    }

    @Test
    public void testTryLock() throws SQLException
    {
        createNode();
        for ( int i = 0; i < 15; i++ )
        {
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery( "MATCH (n:Root {name:'root'}) RETURN n" );
            if ( rs1.next() )
            {
                Object value = rs1.getObject( "n" );
                System.out.println( i + ". value = " + value );
            }
            rs1.close();
            stmt1.close();
        }
    }
}

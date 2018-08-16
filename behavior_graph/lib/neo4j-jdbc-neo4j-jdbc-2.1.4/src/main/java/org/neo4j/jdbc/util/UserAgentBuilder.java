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

package org.neo4j.jdbc.util;

import java.util.Properties;

public class UserAgentBuilder
{
    public static final String USER_AGENT = "userAgent";
    private final Properties jdbcDriverProperties;

    public UserAgentBuilder( Properties jdbcDriverProperties )
    {
        this.jdbcDriverProperties = jdbcDriverProperties;
    }

    public String getAgent()
    {
        StringBuilder sb = new StringBuilder();
        if ( jdbcDriverProperties.containsKey( USER_AGENT ) )
        {
            sb.append( jdbcDriverProperties.getProperty( USER_AGENT ) ).append( " via " );
        }
        sb.append( getImplementationTitle() );
        sb.append( "/" );
        sb.append( getImplementationVersion() );
        return sb.toString();
    }

    private String getImplementationVersion()
    {
        String implementationVersion = getClass().getPackage().getImplementationVersion();
        if ( implementationVersion == null )
        {
            implementationVersion = "<unversioned>";
        }
        return implementationVersion;
    }

    private String getImplementationTitle()
    {
        String implementationTitle = getClass().getPackage().getImplementationTitle();
        if ( implementationTitle == null )
        {
            implementationTitle = "Neo4j JDBC Driver";
        }
        return implementationTitle;
    }
}

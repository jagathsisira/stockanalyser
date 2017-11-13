package com.ucsc.mcs.impl.connector;

import java.sql.Connection;

/**
 * Created by JagathA on 8/11/2017.
 */
public interface SqlConnector {

    boolean loadConfig();

    Connection connect();

    boolean releaseConnection(Connection conn);
}

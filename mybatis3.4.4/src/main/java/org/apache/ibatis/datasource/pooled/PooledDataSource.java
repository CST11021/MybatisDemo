/**
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.datasource.pooled;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * This is a simple, synchronous, thread-safe database connection pool.
 *
 * @author Clinton Begin
 *
带连接池的PooledDataSource

    为什么要使用带连接池的数据源呢，最根本的原因还是因为每次创建连接开销比较大，频繁的创建和关闭数据库连接将会严重的影响
性能。因此，常用的做法是维护一个数据库连接池，每次使用完之后并不是直接关闭数据库连接，再后面如果需要创建数据库连接的时候
直接拿之前释放的数据库连接使用，避免频繁创建和关闭数据库连接造成的开销。

    在mybatis中，定义了一个数据库连接池状态的类PoolState，在这个类里，除维护了数据源实例，还维护着数据库连接。数据库连接
被分成了两种状态类型并存放在两个列表中：idleConnections和activeConnections。

idleConnections:
        空闲(idle)状态PooledConnection对象被放置到此集合中，表示当前闲置的没有被使用的PooledConnection集合，调用PooledDataSource
    的getConnection()方法时，会优先从此集合中取PooledConnection对象。当用完一个java.sql.Connection对象时，MyBatis会将其包
    裹成PooledConnection对象放到此集合中。

activeConnections:
        活动(active)状态的PooledConnection对象被放置到名为activeConnections的ArrayList中，表示当前正在被使用的PooledConnection
    集合，调用PooledDataSource的getConnection()方法时，会优先从idleConnections集合中取PooledConnection对象,如果没有，则
    看此集合是否已满，如果未满，PooledDataSource会创建出一个PooledConnection，添加到此集合中，并返回。

 *
 */
public class PooledDataSource implements DataSource {

    private static final Log log = LogFactory.getLog(PooledDataSource.class);

    private final PoolState state = new PoolState(this);

    private final UnpooledDataSource dataSource;

    /** -- OPTIONAL CONFIGURATION FIELDS -- */
    protected int poolMaximumActiveConnections = 10;
    protected int poolMaximumIdleConnections = 5;
    protected int poolMaximumCheckoutTime = 20000;
    protected int poolTimeToWait = 20000;
    protected String poolPingQuery = "NO PING QUERY SET";
    protected boolean poolPingEnabled;
    protected int poolPingConnectionsNotUsedFor;

    private int expectedConnectionTypeCode;

    public PooledDataSource() {
        dataSource = new UnpooledDataSource();
    }

    public PooledDataSource(UnpooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PooledDataSource(String driver, String url, String username, String password) {
        dataSource = new UnpooledDataSource(driver, url, username, password);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
            dataSource.getPassword());
    }

    public PooledDataSource(String driver, String url, Properties driverProperties) {
        dataSource = new UnpooledDataSource(driver, url, driverProperties);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
            dataSource.getPassword());
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username,
                            String password) {
        dataSource = new UnpooledDataSource(driverClassLoader, driver, url, username, password);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
            dataSource.getPassword());
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        dataSource = new UnpooledDataSource(driverClassLoader, driver, url, driverProperties);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
            dataSource.getPassword());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        dataSource.setDefaultTransactionIsolationLevel(defaultTransactionIsolationLevel);
        forceCloseAll();
    }

    public void setDriverProperties(Properties driverProps) {
        dataSource.setDriverProperties(driverProps);
        forceCloseAll();
    }

    /**
     * The maximum number of active connections
     *
     * @param poolMaximumActiveConnections The maximum number of active connections
     */
    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
        forceCloseAll();
    }

    /**
     * The maximum number of idle connections
     *
     * @param poolMaximumIdleConnections The maximum number of idle connections
     */
    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
        forceCloseAll();
    }

    /**
     * The maximum time a connection can be used before it *may* be
     * given away again.
     *
     * @param poolMaximumCheckoutTime The maximum time
     */
    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
        forceCloseAll();
    }

    /**
     * The time to wait before retrying to get a connection
     *
     * @param poolTimeToWait The time to wait
     */
    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
        forceCloseAll();
    }

    /**
     * The query to be used to check a connection
     *
     * @param poolPingQuery The query
     */
    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
        forceCloseAll();
    }

    /**
     * Determines if the ping query should be used.
     *
     * @param poolPingEnabled True if we need to check a connection before using it
     */
    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
        forceCloseAll();
    }

    /**
     * If a connection has not been used in this many milliseconds, ping the
     * database to make sure the connection is still good.
     *
     * @param milliseconds the number of milliseconds of inactivity that will trigger a ping
     */
    public void setPoolPingConnectionsNotUsedFor(int milliseconds) {
        this.poolPingConnectionsNotUsedFor = milliseconds;
        forceCloseAll();
    }

    public String getDriver() {
        return dataSource.getDriver();
    }

    public String getUrl() {
        return dataSource.getUrl();
    }

    public String getUsername() {
        return dataSource.getUsername();
    }

    public String getPassword() {
        return dataSource.getPassword();
    }

    public boolean isAutoCommit() {
        return dataSource.isAutoCommit();
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return dataSource.getDefaultTransactionIsolationLevel();
    }

    public Properties getDriverProperties() {
        return dataSource.getDriverProperties();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    /**
     * Closes all active and idle connections in the pool
     */
    public void forceCloseAll() {
        synchronized (state) {
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
                dataSource.getPassword());
            for (int i = state.activeConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.activeConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    realConn.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            for (int i = state.idleConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.idleConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    realConn.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("PooledDataSource forcefully closed/removed all connections.");
        }
    }

    public PoolState getPoolState() {
        return state;
    }

    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    protected void pushConnection(PooledConnection conn) throws SQLException {

        synchronized (state) {
            state.activeConnections.remove(conn);
            if (conn.isValid()) {
                if (state.idleConnections.size() < poolMaximumIdleConnections
                    && conn.getConnectionTypeCode() == expectedConnectionTypeCode) {
                    state.accumulatedCheckoutTime += conn.getCheckoutTime();
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
                    state.idleConnections.add(newConn);
                    newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
                    newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
                    conn.invalidate();
                    if (log.isDebugEnabled()) {
                        log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
                    }
                    state.notifyAll();
                } else {
                    state.accumulatedCheckoutTime += conn.getCheckoutTime();
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    conn.getRealConnection().close();
                    if (log.isDebugEnabled()) {
                        log.debug("Closed connection " + conn.getRealHashCode() + ".");
                    }
                    conn.invalidate();
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("A bad connection (" + conn.getRealHashCode()
                        + ") attempted to return to the pool, discarding connection.");
                }
                state.badConnectionCount++;
            }
        }
    }

    /**
     * 我们看下上面的方法都做了什么：
     *      1.  先看是否有空闲(idle)状态下的PooledConnection对象，如果有，就直接返回一个可用的PooledConnection对象；否则进行第2步。
     *      2. 查看活动状态的PooledConnection池activeConnections是否已满；如果没有满，则创建一个新的PooledConnection对象，
     *      然后放到activeConnections池中，然后返回此PooledConnection对象；否则进行第三步；
     *      3.  看最先进入activeConnections池中的PooledConnection对象是否已经过期：如果已经过期，从activeConnections池中
     *      移除此对象，然后创建一个新的PooledConnection对象，添加到activeConnections中，然后将此对象返回；否则进行第4步；
     *      4.  线程等待，循环2步。
     *
     *
     * @param username
     * @param password
     * @return
     * @throws SQLException
     */
    private PooledConnection popConnection(String username, String password) throws SQLException {
        boolean countedWait = false;
        PooledConnection conn = null;
        long t = System.currentTimeMillis();
        int localBadConnectionCount = 0;

        while (conn == null) {
            synchronized (state) {
                // 有闲置的连接对象：
                if (!state.idleConnections.isEmpty()) {
                    // Pool has available connection
                    conn = state.idleConnections.remove(0);
                    if (log.isDebugEnabled()) {
                        log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
                    }
                }

                // 连接对象都被用完了，没有闲置的连接
                else {
                    // Pool does not have available connection
                    // 活动的连接对象小于最大连接数时：
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        // Can create new connection
                        conn = new PooledConnection(dataSource.getConnection(), this);
                        if (log.isDebugEnabled()) {
                            log.debug("Created connection " + conn.getRealHashCode() + ".");
                        }
                    }

                    // 连接对象已经达到顶峰的情况：
                    else {
                        // Cannot create new connection
                        PooledConnection oldestActiveConnection = state.activeConnections.get(0);
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();

                        // 看最先进入activeConnections池中的PooledConnection对象是否已经过期：如果已经过期，
                        // 从activeConnections池中移除此对象，然后创建一个新的PooledConnection对象，添加到activeConnections中，然后将此对象返回
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            // Can claim overdue connection
                            state.claimedOverdueConnectionCount++;
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            state.accumulatedCheckoutTime += longestCheckoutTime;
                            state.activeConnections.remove(oldestActiveConnection);
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                                try {
                                    oldestActiveConnection.getRealConnection().rollback();
                                } catch (SQLException e) {
                                    log.debug("Bad connection. Could not roll back");
                                }
                            }
                            conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                            conn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
                            conn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());
                            oldestActiveConnection.invalidate();
                            if (log.isDebugEnabled()) {
                                log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
                            }
                        }

                        // 这种情况就进行线程等待了，等待其他连接对象释放
                        else {
                            // Must wait
                            try {
                                if (!countedWait) {
                                    state.hadToWaitCount++;
                                    countedWait = true;
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                                }
                                long wt = System.currentTimeMillis();
                                state.wait(poolTimeToWait);
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }


                    }
                }

                if (conn != null) {
                    if (conn.isValid()) {
                        if (!conn.getRealConnection().getAutoCommit()) {
                            conn.getRealConnection().rollback();
                        }
                        conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        state.activeConnections.add(conn);
                        state.requestCount++;
                        state.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("A bad connection (" + conn.getRealHashCode()
                                + ") was returned from the pool, getting another connection.");
                        }
                        state.badConnectionCount++;
                        localBadConnectionCount++;
                        conn = null;
                        if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            if (log.isDebugEnabled()) {
                                log.debug("PooledDataSource: Could not get a good connection to the database.");
                            }
                            throw new SQLException(
                                "PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }

        }

        if (conn == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "PooledDataSource: Unknown severe error condition.  The connection pool returned a null "
                        + "connection.");
            }
            throw new SQLException(
                "PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }

        return conn;
    }

    /**
     * Method to check to see if a connection is still usable
     *
     * @param conn - the connection to check
     * @return True if the connection is still usable
     */
    protected boolean pingConnection(PooledConnection conn) {
        boolean result = true;

        try {
            result = !conn.getRealConnection().isClosed();
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            }
            result = false;
        }

        if (result) {
            if (poolPingEnabled) {
                if (poolPingConnectionsNotUsedFor >= 0
                    && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Testing connection " + conn.getRealHashCode() + " ...");
                        }
                        Connection realConn = conn.getRealConnection();
                        Statement statement = realConn.createStatement();
                        ResultSet rs = statement.executeQuery(poolPingQuery);
                        rs.close();
                        statement.close();
                        if (!realConn.getAutoCommit()) {
                            realConn.rollback();
                        }
                        result = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
                        }
                    } catch (Exception e) {
                        log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try {
                            conn.getRealConnection().close();
                        } catch (Exception e2) {
                            //ignore
                        }
                        result = false;
                        if (log.isDebugEnabled()) {
                            log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Unwraps a pooled connection to get to the 'real' connection
     *
     * @param conn - the pooled connection to unwrap
     * @return The 'real' connection
     */
    public static Connection unwrapConnection(Connection conn) {
        if (Proxy.isProxyClass(conn.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(conn);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection)handler).getRealConnection();
            }
        }
        return conn;
    }

    protected void finalize() throws Throwable {
        forceCloseAll();
        super.finalize();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // requires JDK version 1.6
    }

}

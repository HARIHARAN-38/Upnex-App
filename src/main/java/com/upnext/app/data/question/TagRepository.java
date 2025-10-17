package com.upnext.app.data.question;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.domain.question.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TagRepository {
    private static final Logger LOGGER = Logger.getInstance();
    private static final TagRepository INSTANCE = new TagRepository();

    private static final String INSERT_SQL =
            "INSERT INTO tags (name, usage_count) VALUES (?, ?)";
    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, usage_count FROM tags WHERE id = ?";
    private static final String FIND_BY_NAME_SQL =
            "SELECT id, name, usage_count FROM tags WHERE name = ?";
    private static final String UPDATE_SQL =
            "UPDATE tags SET name = ?, usage_count = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM tags WHERE id = ?";
    private static final String FIND_ALL_SQL =
            "SELECT id, name, usage_count FROM tags ORDER BY name";
    private static final String FIND_TRENDING_SQL =
            "SELECT id, name, usage_count FROM tags ORDER BY usage_count DESC LIMIT ?";

    private TagRepository() {
    }

    public static TagRepository getInstance() {
        return INSTANCE;
    }

    public Tag save(Tag tag) throws SQLException {
        Objects.requireNonNull(tag, "tag");
        String name = normalizeName(tag.getName());
        tag.setName(name);

        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;
        ResultSet keys = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.setInt(2, Math.max(0, tag.getUsageCount()));

                if (statement.executeUpdate() == 0) {
                    throw new SQLException("No rows inserted for tag");
                }

                keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    tag.setId(keys.getLong(1));
                } else {
                    throw new SQLException("Missing generated id for tag");
                }
            }
            return tag;
        } catch (SQLException ex) {
            LOGGER.logException("Failed to save tag: " + name, ex);
            throw ex;
        } finally {
            closeQuietly(keys);
            releaseConnection(provider, connection);
        }
    }

    public Optional<Tag> findById(Long id) throws SQLException {
        Objects.requireNonNull(id, "id");

        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            LOGGER.logException("Failed to find tag by id: " + id, ex);
            throw ex;
        } finally {
            releaseConnection(provider, connection);
        }
    }

    public Optional<Tag> findByName(String name) throws SQLException {
        String normalized = normalizeName(name);

        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(FIND_BY_NAME_SQL)) {
                statement.setString(1, normalized);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            LOGGER.logException("Failed to find tag by name: " + normalized, ex);
            throw ex;
        } finally {
            releaseConnection(provider, connection);
        }
    }

    public boolean update(Tag tag) throws SQLException {
        Objects.requireNonNull(tag, "tag");
        if (tag.getId() == null) {
            throw new IllegalArgumentException("Tag id must not be null");
        }
        String name = normalizeName(tag.getName());
        tag.setName(name);

        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setString(1, name);
                statement.setInt(2, Math.max(0, tag.getUsageCount()));
                statement.setLong(3, tag.getId());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            LOGGER.logException("Failed to update tag: " + tag.getId(), ex);
            throw ex;
        } finally {
            releaseConnection(provider, connection);
        }
    }

    public boolean delete(Long id) throws SQLException {
        Objects.requireNonNull(id, "id");

        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setLong(1, id);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            LOGGER.logException("Failed to delete tag: " + id, ex);
            throw ex;
        } finally {
            releaseConnection(provider, connection);
        }
    }

    public List<Tag> findAll() throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
                 ResultSet rs = statement.executeQuery()) {
                List<Tag> tags = new ArrayList<>();
                while (rs.next()) {
                    tags.add(mapRow(rs));
                }
                return tags;
            }
        } catch (SQLException ex) {
            LOGGER.logException("Failed to load tags", ex);
            throw ex;
        } finally {
            releaseConnection(provider, connection);
        }
    }

    public List<Tag> findTrendingTags(int limit) throws SQLException {
        int safeLimit = Math.max(1, limit);

        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = null;

        try {
            connection = provider.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(FIND_TRENDING_SQL)) {
                statement.setInt(1, safeLimit);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Tag> tags = new ArrayList<>();
                    while (rs.next()) {
                        tags.add(mapRow(rs));
                    }
                    return tags;
                }
            }
        } catch (SQLException ex) {
            LOGGER.logException("Failed to load trending tags", ex);
            throw ex;
        } finally {
            releaseConnection(provider, connection);
        }
    }

    private Tag mapRow(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        tag.setUsageCount(rs.getInt("usage_count"));
        return tag;
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Tag name must not be null");
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        return normalized;
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
            // nothing to do
        }
    }

    private void releaseConnection(JdbcConnectionProvider provider, Connection connection) {
        if (connection != null) {
            provider.releaseConnection(connection);
        }
    }
}
package ucles.weblab.common.domain.jpa.hibernate;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * Overrides the default Hibernate dialect for PostgreSQL such that binary data are correctly handled in BYTEA columns.
 * <p>
 * Use this by specifying the appropriate application property e.g.
 * <dl>
 *     <dt>spring.jpa.database-platform</dt>
 *     <dd>ucles.weblab.common.domain.jpa.hibernate.PostgreSQL9DialectNoBlobs</dd>
 * </dl>
 *
 * @since 09/07/15
 */
public class PostgreSQL9DialectNoBlobs extends PostgreSQL9Dialect {
    @Override
    public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
        if (sqlTypeDescriptor.getSqlType() == java.sql.Types.BLOB) {
            return BinaryTypeDescriptor.INSTANCE;
        }
        return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
    }
}

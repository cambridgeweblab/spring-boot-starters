package ucles.weblab.common.test.domain.jpa;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Static utility methods to make Hibernate Proxies less painful.
 *
 * @since 04/08/2014
 */
public class JpaUtils {
    private static final Logger log = LoggerFactory.getLogger(JpaUtils.class);

    private JpaUtils() { // Prevent instantiation
    }

    private interface EntityStrategy<T> {
        Serializable getId(T entity);
    }

    private static class PersistableEntityStrategy<T extends Persistable<ID>,ID extends Serializable> implements EntityStrategy<T> {
        @Override
        public Serializable getId(T entity) {
            return entity.getId();
        }
    }

    private static class PersistenceUnitUtilEntityStrategy<T> implements EntityStrategy<T> {
        private final EntityManagerFactory entityManagerFactory;

        public PersistenceUnitUtilEntityStrategy(EntityManagerFactory entityManagerFactory) {
            this.entityManagerFactory = entityManagerFactory;
        }

        @Override
        public Serializable getId(T entity) {
            return (Serializable) entityManagerFactory.getPersistenceUnitUtil().getIdentifier(entity);
        }
    }

    public static <ID extends Serializable, T extends Persistable<ID>> T ensureManaged(EntityManager em, final T entity) {
        return ensureManaged(em, entity, new PersistableEntityStrategy<>());
    }

    public static <T> T ensureManaged(EntityManager em, final T entity) {
        return ensureManaged(em, entity, new PersistenceUnitUtilEntityStrategy<T>(em.getEntityManagerFactory()));
    }

    private static <T> T ensureManaged(EntityManager em, T entity, EntityStrategy<T> strategy) {
        final Class<? extends T> entityClass;
        final Serializable entityId;

        if (entity != null) {
            if (entity instanceof HibernateProxy) {
                HibernateProxy proxy = (HibernateProxy) entity;
                LazyInitializer li = proxy.getHibernateLazyInitializer();
                entityClass = li.getPersistentClass();
                entityId = li.getIdentifier();
            } else {
                entityClass = (Class<? extends T>) entity.getClass();
                entityId = strategy.getId(entity);
            }
            if (em == null) {
                log.warn("EntityManager null in ensureManaged() - cannot ensure {0} is managed.", entity.getClass());
            } else {
                if (!em.contains(entity)) {
                    return em.find(entityClass, entityId);
                }
            }
        }

        return entity;
    }

}

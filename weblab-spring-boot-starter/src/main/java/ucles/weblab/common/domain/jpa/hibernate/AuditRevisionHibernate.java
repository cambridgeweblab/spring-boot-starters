package ucles.weblab.common.domain.jpa.hibernate;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Used by Hibernate Envers to store revisions.
 *
 * @since 21/07/15
 */
@Entity(name = "AuditRevision")
@Table(name = "audit_revisions")
@RevisionEntity(AuditRevisionHibernate.RevisionListener.class)
public class AuditRevisionHibernate implements Persistable<Long> {
    @Id
    @GeneratedValue(generator="auditGenerator", strategy = GenerationType.TABLE)
    @GenericGenerator(name = "auditGenerator", strategy = "org.hibernate.id.enhanced.TableGenerator",
            parameters = {
                    @Parameter(name = "increment_size", value = "1"),
                    @Parameter(name = "optimizer", value = "none"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "segment_value", value = "audit")
            })
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    @Column(name = "instant_millis")
    private long epochMilli;

    private String username;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(epochMilli);
    }

    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    public static class RevisionListener implements org.hibernate.envers.RevisionListener {
        @Override
        public void newRevision(Object o) {
            final AuditRevisionHibernate auditRevision = (AuditRevisionHibernate) o;
            Optional.ofNullable(SecurityContextHolder.getContext())
                    .map(SecurityContext::getAuthentication)
                    .map(Authentication::getName)
                    .ifPresent(auditRevision::setUsername);
        }
    }
}

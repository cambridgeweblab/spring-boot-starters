/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ucles.weblab.common.domain.jpa.hibernate;

import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A delegate class for the the persistance context. This can be used by the worflow
 * engine to execute commands on the entity manager through the methods declared in 
 * this class. 
 * 
 * 
 * @author Sukhraj
 */
public class JavaPersistenceDelegate {
    
    @Autowired
    private final EntityManager entityManager;
    
    public JavaPersistenceDelegate(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    /**
     * Call flush on the entity manager. 
     * 
     */
    public void flush() {
        entityManager.flush();
    }
}

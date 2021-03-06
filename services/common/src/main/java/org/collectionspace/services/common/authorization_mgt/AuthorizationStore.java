/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.common.authorization_mgt;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.common.authorization_mgt.RoleStorageConstants;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorizationStore stores persistent entities during import
 * @author
 */
public class AuthorizationStore {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationStore.class);
    private final static String PERSISTENCE_UNIT = "org.collectionspace.services.authorization";

    static public Role getRoleByName(String roleName, String tenantId) {
    	Role theRole = null;
    	
    	try {
    		theRole = (Role)JpaStorageUtils.getEnityByKey(Role.class.getName(),
    				RoleStorageConstants.ROLE_NAME, roleName, tenantId);
    	} catch (Throwable e) {
    		if (logger.isTraceEnabled() == true) {
    			logger.trace("Could not retrieve role with name =" + roleName, e);
    		}
    	}
    	
    	return theRole;
    }
    
    static public Role getRoleByName(EntityManager em, String roleName, String tenantId) {
    	Role theRole = null;
    	
    	try {
    		theRole = (Role)JpaStorageUtils.getEnityByKey(em, Role.class.getName(),
    				RoleStorageConstants.ROLE_NAME, roleName, tenantId);
    	} catch (Throwable e) {
    		if (logger.isTraceEnabled() == true) {
    			logger.trace("Could not retrieve role with name =" + roleName, e);
    		}
    	}
    	
    	return theRole;
    }
    
    
    static public Permission getPermission(Permission permission) {
    	Permission result = null;
    	//
    	// We need to perform a DB lookup to see if this permission already exists.  If so,
    	// we should return the existing permission.
    	//
    	result = permission;
    	
    	return result;
    }
    
    /**
     * store the given entity
     * @param entity
     * @return csid of the entity
     * @throws Exception
     */
    public String store(Object entity) throws Exception {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = JpaStorageUtils.getEntityManagerFactory(PERSISTENCE_UNIT);
            em = emf.createEntityManager();
            //FIXME: more efficient would be to participate in transaction already started
            //by the caller
            em.getTransaction().begin();
            if (JaxbUtils.getValue(entity, "getCreatedAt") == null) {
                JaxbUtils.setValue(entity, "setCreatedAtItem", Date.class, new Date());
            }
            em.persist(entity);
            em.getTransaction().commit();
            String id = null;
            try{
                id = (String) JaxbUtils.getValue(entity, "getCsid"); //NOTE: Not all entities have a CSID attribute
            } catch(NoSuchMethodException nsme) {
                //do nothing ok, relationship does not have csid
            }
            return id;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw e;
        } finally {
            if (em != null) {
            	em.clear();
            	em.close();
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }
    
    private boolean exists(EntityManager em, Object entity) {
    	boolean result = false;
    	
    	try {
    		String csid = (String)JaxbUtils.getValue(entity, "getCsid");
    		Object existingEntity = em.find(entity.getClass(), csid);
    		if (existingEntity != null) {
    			result = true;
    		}
    	} catch (Exception e) {
    		//NOTE: Not all entities have a CSID attribute
    	}
    	
    	return result;
    }
    /*
     * Use this method if you've already started a transaction with an EntityManager
     */
    public String store(EntityManager em, Object entity) throws Exception {
    	boolean entityExists = exists(em, entity);
    	if (entityExists == true) {
    		logger.debug("Entity to persist already exists.");
    	}
        if (JaxbUtils.getValue(entity, "getCreatedAt") == null) {
            JaxbUtils.setValue(entity, "setCreatedAtItem", Date.class, new Date());
        }
        
        if (entityExists == true) {
        	//em.merge(entity); FIXME: Leave commented out until we address CSPACE-5031
        } else {
        	em.persist(entity);
        }
        
        // look for a CSID
        String id = null;
        try{
            id = (String) JaxbUtils.getValue(entity, "getCsid"); //NOTE: Not all entities have a CSID attribute
        } catch(NoSuchMethodException nsme) {
            //do nothing ok, relationship does not have csid
        }
        return id;
    }
}

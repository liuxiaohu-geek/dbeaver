/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.generic.model.meta;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.meta.GenericMetaModel;
import org.jkiss.dbeaver.ext.generic.model.meta.GenericMetaObject;
import org.jkiss.dbeaver.model.impl.AbstractDescriptor;
import org.jkiss.dbeaver.registry.RegistryConstants;
import org.jkiss.utils.ArrayUtils;
import org.jkiss.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public class GenericMetaModelDescriptor extends AbstractDescriptor {

    private IConfigurationElement contributorConfig;
    private ObjectType implType;
    private GenericMetaModel instance;

    private String id;
    private final Map<String, GenericMetaObject> objects = new HashMap<>();
    private String[] driverClass;

    public GenericMetaModelDescriptor() {
        super("org.jkiss.dbeaver.ext.generic");
        implType = new ObjectType(GenericMetaModel.class.getName());
        instance = new GenericMetaModel();
        instance.descriptor = this;
    }

    public GenericMetaModelDescriptor(IConfigurationElement cfg) {
        super(cfg);
        this.contributorConfig = cfg;

        this.id = cfg.getAttribute(RegistryConstants.ATTR_ID);
        IConfigurationElement[] objectList = cfg.getChildren("object");
        if (!ArrayUtils.isEmpty(objectList)) {
            for (IConfigurationElement childConfig : objectList) {
                GenericMetaObject metaObject = new GenericMetaObject(childConfig);
                objects.put(metaObject.getType(), metaObject);
            }
        }
        String driverClassList = cfg.getAttribute("driverClass");
        if (CommonUtils.isEmpty(driverClassList)) {
            this.driverClass = new String[0];
        } else {
            this.driverClass = driverClassList.split(",");
        }

        implType = new ObjectType(cfg.getAttribute("class"));
    }

    public String getId()
    {
        return id;
    }

    @NotNull
    public String[] getDriverClass() {
        return driverClass;
    }

    public GenericMetaObject getObject(String id)
    {
        return objects.get(id);
    }

    public GenericMetaModel getInstance() throws DBException {
        if (instance != null) {
            return instance;
        }
        Class<? extends GenericMetaModel> implClass = implType.getObjectClass(GenericMetaModel.class);
        if (implClass == null) {
            throw new DBException("Can't create generic meta model instance '" + implType.getImplName() + "'");
        }
        try {
            instance = implClass.newInstance();
        } catch (Throwable e) {
            throw new DBException("Can't instantiate meta model", e);
        }
        instance.descriptor = this;
        return instance;
    }

}

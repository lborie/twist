/**
 *
 * Copyright (c) 2014 Kerby Martino and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  __            __       __
 * |  |_.--.--.--|__.-----|  |_
 * |   _|  |  |  |  |__ --|   _|
 * |____|________|__|_____|____|
 * :: Twist :: Object Mapping ::
 *
 */
package com.textquo.twist.types;

import com.google.appengine.api.datastore.DatastoreService;
import com.textquo.twist.GaeObjectStore;
import com.textquo.twist.object.QueryStore;
import com.textquo.twist.util.Pair;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

import java.util.*;

/**
 * Builds GAE filters and sorts
 */
public class Find<V> {

    protected Map<String, Pair<Query.FilterOperator, Object>> filters;
    protected Map<String, Query.SortDirection> sorts;
    protected List<String> projections;
    protected Integer skip;
    protected Integer max;
    protected boolean keysOnly = false;

    final GaeObjectStore objectStore;
    final QueryStore _store;
    final Class<V> _clazz;
    final String _kind;

    public Find(GaeObjectStore store, Class<V> clazz, String kind){
        filters = new LinkedHashMap<String, Pair<Query.FilterOperator, Object>>();
        sorts = new LinkedHashMap<String, Query.SortDirection>();
        projections = new LinkedList<String>();
        objectStore = store;
        _store = new QueryStore(store.getDatastoreService(), null);
        _clazz = clazz;
        _kind = kind;
    }

    public Find greaterThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Find greaterThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN_OR_EQUAL, value));
        return this;
    }

    public Find lessThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
        return this;
    }

    public Find lessThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN_OR_EQUAL, value));
        return this;
    }

    public Find in(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.IN, value));
        return this;
    }

    public Find equal(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.EQUAL, value));
        return this;
    }

    public Find notEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.NOT_EQUAL, value));
        return this;
    }

    public Find sortAscending(String key){
        sorts.put(key, Query.SortDirection.ASCENDING);
        return this;
    }

    public Find sortDescending(String key){
        sorts.put(key, Query.SortDirection.DESCENDING);
        return this;
    }

    public Find skip(int skip){
        this.skip = skip;
        return this;
    }

    public Find limit(int limit){
        this.max = limit;
        return this;
    }

    public Find keysOnly(){
        this.keysOnly = true;
        return this;
    }

    public Find projection(String field){
        throw new RuntimeException("Not yet implemented");
    }

    public Find projection(String[]fields){
        throw new RuntimeException("Not yet implemented");
    }

    public Find projection(Iterable<String> fields){
        throw new RuntimeException("Not yet implemented");
    }

    public Iterator<V> now() {
        if (filters == null){
            filters = new HashMap<String, Pair<Query.FilterOperator, Object>>();
        }
        /**
         * Map of fields and its matching filter operator and compare valueType
         */
        Iterator<V> it = null;
        try {
            if (sorts == null){
                sorts = new HashMap<String, Query.SortDirection>();
            }
            final Iterator<Entity> eit = _store.querySortedLike(_kind, filters, sorts, max, skip, keysOnly);
            it = new Iterator<V>() {
                public void remove() {
                    eit.remove();
                }
                public V next() {
                    Entity e = eit.next();
                    V instance = null;
                    if(_clazz.equals(Map.class)){
                        instance = (V) new LinkedHashMap<>();
                    } else {
                        instance = createInstance(_clazz);
                    }
                    objectStore.unmarshaller().unmarshall(instance, e);
                    return instance;
                }
                public boolean hasNext() {
                    return eit.hasNext();
                }
            };
        } catch (Exception e) {
            // TODO Handle exception
            e.printStackTrace();
            it = null;
        } finally {

        }
        return it;
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

}

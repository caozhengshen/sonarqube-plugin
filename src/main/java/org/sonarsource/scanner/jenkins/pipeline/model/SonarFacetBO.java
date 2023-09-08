/*
 * SonarQube Scanner for Jenkins
 * Copyright (C) 2007-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.scanner.jenkins.pipeline.model;

import groovy.transform.ToString;

import java.util.List;

/**
 * @author caozhensheng
 * @date 2023/9/5
 */
public class SonarFacetBO {
    /**
     * 属性
     */
    private String      property;

    /**
     * 类型-数值列表
     */
    private List<Value> values;

    @ToString
    public class Value {
        /**
         * 名称
         */
        private String  val;

        /**
         * 数值
         */
        private Integer count;

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }
}

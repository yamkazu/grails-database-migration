/*
 * Copyright 2015 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugins.databasemigration.command

import groovy.transform.CompileStatic
import liquibase.Liquibase
import org.grails.plugins.databasemigration.DatabaseMigrationException
import org.grails.plugins.databasemigration.ScriptDatabaseMigrationCommand

@CompileStatic
class DbmUpdateCountCommand implements ScriptDatabaseMigrationCommand {

    void handle() {
        def number = args[0]
        if (!number) {
            throw new DatabaseMigrationException("The $name command requires a change set number argument")
        }
        if (!number.isNumber()) {
            throw new DatabaseMigrationException("The change set number argument '$number' isn't a number")
        }

        def contexts = optionValue('contexts')
        def defaultSchema = optionValue('defaultSchema')
        def dataSource = optionValue('dataSource')

        withLiquibase(defaultSchema, dataSource) { Liquibase liquibase ->
            liquibase.update(number.toInteger(), contexts)
        }
    }
}
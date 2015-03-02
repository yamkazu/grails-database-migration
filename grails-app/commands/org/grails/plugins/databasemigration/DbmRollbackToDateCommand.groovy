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
package org.grails.plugins.databasemigration

import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ExecutionContext
import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import liquibase.Liquibase

import java.text.ParseException

@Commons
@CompileStatic
class DbmRollbackToDateCommand implements ApplicationCommand, DatabaseMigrationCommand {

    final String description = 'Rolls back the database to the state it was in at the given date/time'

    @Override
    boolean handle(ExecutionContext executionContext) {
        def commandLine = executionContext.commandLine

        def dateStr = commandLine.remainingArgs[0]
        if (!dateStr) {
            log.error 'Date must be specified as two strings with the format "yyyy-MM-dd HH:mm:ss" or as one strings with the format "yyyy-MM-dd"'
            return false
        }

        def timeStr = commandLine.remainingArgs[1]

        def date = null
        try {
            date = parseDateTime(dateStr, timeStr)
        } catch (ParseException e) {
            log.error "Problem parsing '$dateStr${timeStr ? " $timeStr" : ''}' as a Date: $e.message"
            return false
        }

        def contexts = commandLine.optionValue('contexts') as String
        def defaultSchema = commandLine.optionValue('defaultSchema') as String
        def dataSource = commandLine.optionValue('dataSource') as String

        withLiquibase(changeLogFile, defaultSchema, getDataSourceConfig(dataSource)) { Liquibase liquibase ->
            liquibase.rollback(date, contexts)
        }

        return true
    }
}
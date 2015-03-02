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

class DbmRollbackToDateCommandSpec extends DatabaseMigrationCommandSpec {

    final Class<ApplicationCommand> commandClass = DbmRollbackToDateCommand

    def setup() {
        command.changeLogFile << CHANGE_LOG_CONTENT

        new DbmUpdateCommand(applicationContext: applicationContext).handle(getExecutionContext())
        sql.executeUpdate('UPDATE PUBLIC.DATABASECHANGELOG SET DATEEXECUTED = \'2015-01-02 12:00:00\' WHERE ID = \'1\'')

        def tables = sql.rows('SELECT table_name FROM information_schema.tables WHERE table_type = \'TABLE\'').collect { it.table_name.toLowerCase() }
        assert tables as Set == ['book', 'author', 'databasechangeloglock', 'databasechangelog'] as Set
    }

    def "rolls back the database to the state it was in at the given date/time"() {
        when:
            command.handle(getExecutionContext(args as String[]))

        then:
            def tables = sql.rows('SELECT table_name FROM information_schema.tables WHERE table_type = \'TABLE\'').collect { it.table_name.toLowerCase() }
            tables as Set == ['author', 'databasechangeloglock', 'databasechangelog'] as Set

        where:
            args << [['2015-01-03'], ['2015-01-02', '13:00:00']]
    }

    def "an error occurs if the date parameter is not specified"() {
        when:
            def result = command.handle(getExecutionContext())

        then:
            !result
            outputCapture.toString().contains('Date must be specified as two strings with the format "yyyy-MM-dd HH:mm:ss" or as one strings with the format "yyyy-MM-dd"')
    }

    def "an error occurs if the date parameter is invalid format"() {
        when:
            def result = command.handle(getExecutionContext(args as String[]))

        then:
            !result
            outputCapture.toString().contains("Problem parsing '${args.join(' ')}' as a Date")

        where:
            args << [['XXXX-01-03'], ['XXXX-01-02', '13:00:00']]
    }

    static final String CHANGE_LOG_CONTENT = '''
databaseChangeLog:
- changeSet:
    id: 1
    author: John Smith
    changes:
    - createTable:
        columns:
        - column:
            autoIncrement: true
            constraints:
              constraints:
                primaryKey: true
                primaryKeyName: authorPK
            name: id
            type: BIGINT
        - column:
            constraints:
              constraints:
                nullable: false
            name: version
            type: BIGINT
        - column:
            constraints:
              constraints:
                nullable: false
            name: name
            type: VARCHAR(255)
        tableName: author
- changeSet:
    id: 2
    author: John Smith
    changes:
    - createTable:
        columns:
        - column:
            autoIncrement: true
            constraints:
              constraints:
                primaryKey: true
                primaryKeyName: bookPK
            name: id
            type: BIGINT
        - column:
            constraints:
              constraints:
                nullable: false
            name: version
            type: BIGINT
        - column:
            constraints:
              constraints:
                nullable: false
            name: author_id
            type: BIGINT
        - column:
            constraints:
              constraints:
                nullable: false
            name: title
            type: VARCHAR(255)
        tableName: book
'''
}
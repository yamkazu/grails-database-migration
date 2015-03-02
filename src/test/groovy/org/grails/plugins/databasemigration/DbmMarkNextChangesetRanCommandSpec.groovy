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

class DbmMarkNextChangesetRanCommandSpec extends DatabaseMigrationCommandSpec {

    final Class<ApplicationCommand> commandClass = DbmMarkNextChangesetRanCommand

    def "marks the next change changes as executed in the database"() {
        given:
            command.changeLogFile << CHANGE_LOG_CONTENT

        when:
            command.handle(getExecutionContext())

        then:
            def rows = sql.rows('SELECT id FROM databasechangelog').collect { it.id }
            rows == ['changeSet1']
    }

    static final String CHANGE_LOG_CONTENT = '''
databaseChangeLog:
  - changeSet:
      id: changeSet1
      author: John Smith
  - changeSet:
      id: changeSet2
      author: John Smith
'''
}

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

import grails.dev.commands.ApplicationCommand
import org.grails.plugins.databasemigration.DatabaseMigrationException

class DbmGenerateGormChangelogCommandSpec extends ApplicationContextDatabaseMigrationCommandSpec {

    final Class<ApplicationCommand> commandClass = DbmGenerateGormChangelogCommand

    def "writes Change Log to copy the current state of the database to STDOUT"() {
        when:
            command.handle(getExecutionContext())

        then:
            outputCapture.toString() =~ '''
<databaseChangeLog xmlns=".+?">
    <changeSet author=".+?" id=".+?">
        <createTable tableName="author">
            <column autoIncrement="true" name="id" type="BIGINT">
                <constraints primaryKey="true" primaryKeyName="authorPK"/>
            </column>
            <column name="version" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="name" type="VARCHAR\\(255\\)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
    <changeSet author=".+?" id=".+?">
        <createTable tableName="book">
            <column autoIncrement="true" name="id" type="BIGINT">
                <constraints primaryKey="true" primaryKeyName="bookPK"/>
            </column>
            <column name="version" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="author_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="title" type="VARCHAR\\(255\\)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
    <changeSet author=".+?" id=".+?">
        <addForeignKeyConstraint baseColumnNames="author_id" baseTableName="book" constraintName="FK_4sac2ubmnqva85r8bk8fxdvbf" deferrable="false" initiallyDeferred="false" referencedColumnNames="id" referencedTableName="author"/>
    </changeSet>
</databaseChangeLog>
'''.trim()
    }

    def "writes Change Log to copy the current state of the database to a file given as arguments"() {
        given:
            def filename = 'changelog.yml'

        when:
            command.handle(getExecutionContext(filename))

        then:
            def output = new File(changeLogLocation, filename).text
            output =~ '''
databaseChangeLog:
- changeSet:
    id: .+?
    author: .+?
    changes:
    - createTable:
        columns:
        - column:
            autoIncrement: true
            constraints:
              primaryKey: true
              primaryKeyName: authorPK
            name: id
            type: BIGINT
        - column:
            constraints:
              nullable: false
            name: version
            type: BIGINT
        - column:
            constraints:
              nullable: false
            name: name
            type: VARCHAR\\(255\\)
        tableName: author
- changeSet:
    id: .+?
    author: .+?
    changes:
    - createTable:
        columns:
        - column:
            autoIncrement: true
            constraints:
              primaryKey: true
              primaryKeyName: bookPK
            name: id
            type: BIGINT
        - column:
            constraints:
              nullable: false
            name: version
            type: BIGINT
        - column:
            constraints:
              nullable: false
            name: author_id
            type: BIGINT
        - column:
            constraints:
              nullable: false
            name: title
            type: VARCHAR\\(255\\)
        tableName: book
- changeSet:
    id: .+?
    author: .+?
    changes:
    - addForeignKeyConstraint:
        baseColumnNames: author_id
        baseTableName: book
        constraintName: FK_4sac2ubmnqva85r8bk8fxdvbf
        deferrable: false
        initiallyDeferred: false
        referencedColumnNames: id
        referencedTableName: author
'''.trim()
    }

    def "an error occurs if changeLogFile already exists"() {
        given:
            def filename = 'changelog.yml'
            def changeLogFile = new File(changeLogLocation, filename)
            assert changeLogFile.createNewFile()

        when:
            command.handle(getExecutionContext(filename))

        then:
            def e = thrown(DatabaseMigrationException)
            e.message == "ChangeLogFile ${changeLogFile.canonicalPath} already exists!"
    }

    @Override
    protected Class[] getDomainClasses() {
        [Book, Author] as Class[]
    }
}
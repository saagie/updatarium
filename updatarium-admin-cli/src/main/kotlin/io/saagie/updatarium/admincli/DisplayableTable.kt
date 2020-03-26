/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2020 Pierre Leresteux.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.saagie.updatarium.admincli

class DisplayableTable(private val headers: List<String>, private val rows: List<List<String>>) {

    private val columnSizes = getColumnSizes(headers, rows)

    private fun printHeader() {
        val rowFormat = columnSizes.indices
            .joinToString(FIELD_SEPARATOR) { "%${columnSizes[it]}s" }
        println(
            rowFormat.format(completeWithString(headers, columnSizes.size)
                .map { it.toUpperCase() }
                .toTypedArray()
            )
        )
    }

    private fun printRows() {
        val rowFormat = columnSizes.indices
            .joinToString(FIELD_SEPARATOR) { "%${columnSizes[it]}s" }
        rows.forEach {
            println(rowFormat.format(completeWithString(it, columnSizes.size).toTypedArray()))
        }
    }

    fun print() {
        printHeader()
        printRows()
    }

    companion object {

        private const val FIELD_SEPARATOR = "\t"

        private fun completeWithString(values: List<String>, expectedSize: Int): List<String> {
            return if (expectedSize - values.size > 0)
                completeWithString(values.plus(""), expectedSize - 1)
                else values
        }

        private fun getColumnSizes(headers: List<String>, rows: List<List<String>>): List<Int> {
            val headerSizes = headers.map { it.length }
            val rowsSizes = rows.map { it.map { it.length } }
            val concatenatedSizes = rowsSizes.plus(listOf(headerSizes))
            val columnNb = concatenatedSizes.map { it.size }.max() ?: 0
            return (0 until columnNb).map { index ->
                concatenatedSizes.map { it.getOrElse(index) { 0 } }.max() ?: 0
            }
        }

    }
}

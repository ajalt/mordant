package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.Padding
import com.github.ajalt.mordant.rendering.RenderingTest
import com.github.ajalt.mordant.rendering.table.Borders.*
import org.junit.Test

class TableBorderStyleTest : RenderingTest() {
    @Test
    fun square() = doTest(BorderStyle.SQUARE, """
        |┌──┬──┐     ╷   
        |│1 │2 │3  4 │5  
        |├──┼──┤  ╶──┼──╴
        |│6 │7 │8  9 │10 
        |└──┴──┘  ╷  ╵  ╷
        | 11 12 13│14 15│
        |┌──┬──┐  ╵  ╷  ╵
        |│16│17│18 19│20 
        |├──┼──┤     │   
        |│21│22│23 24│25 
        |└──┴──┘  ╷  ╵  ╷
        | 26 27 28│29 30│
        |┌──┬──┐  ╵  ╷  ╵
        |│31│32│33 34│35 
        |├──┼──┤  ╶──┼──╴
        |│36│37│38 39│40 
        |└──┴──┘  ╷  ╵  ╷
        | 41 42 43│44 45│
        |┌──┬──┐  ╵  ╷  ╵
        |│46│47│48 49│50 
        |├──┼──┤     │   
        |│51│52│53 54│55 
        |└──┴──┘  ╷  ╵  ╷
        | 56 57 58│59 60│
        |┌──┬──┐  ╵  ╷  ╵
        |│61│62│63 64│65 
        |├──┼──┤  ╶──┼──╴
        |│66│67│68 69│70 
        |└──┴──┘     ╵   
    """)

    @Test
    fun rounded() = doTest(BorderStyle.ROUNDED, """
        |╭──┬──╮     ╷   
        |│1 │2 │3  4 │5  
        |├──┼──┤  ╶──┼──╴
        |│6 │7 │8  9 │10 
        |╰──┴──╯  ╷  ╵  ╷
        | 11 12 13│14 15│
        |╭──┬──╮  ╵  ╷  ╵
        |│16│17│18 19│20 
        |├──┼──┤     │   
        |│21│22│23 24│25 
        |╰──┴──╯  ╷  ╵  ╷
        | 26 27 28│29 30│
        |╭──┬──╮  ╵  ╷  ╵
        |│31│32│33 34│35 
        |├──┼──┤  ╶──┼──╴
        |│36│37│38 39│40 
        |╰──┴──╯  ╷  ╵  ╷
        | 41 42 43│44 45│
        |╭──┬──╮  ╵  ╷  ╵
        |│46│47│48 49│50 
        |├──┼──┤     │   
        |│51│52│53 54│55 
        |╰──┴──╯  ╷  ╵  ╷
        | 56 57 58│59 60│
        |╭──┬──╮  ╵  ╷  ╵
        |│61│62│63 64│65 
        |├──┼──┤  ╶──┼──╴
        |│66│67│68 69│70 
        |╰──┴──╯     ╵   
    """)

    @Test
    fun heavy() = doTest(BorderStyle.HEAVY, """
        |┏━━┳━━┓     ╻   
        |┃1 ┃2 ┃3  4 ┃5  
        |┣━━╋━━┫  ╺━━╋━━╸
        |┃6 ┃7 ┃8  9 ┃10 
        |┗━━┻━━┛  ╻  ╹  ╻
        | 11 12 13┃14 15┃
        |┏━━┳━━┓  ╹  ╻  ╹
        |┃16┃17┃18 19┃20 
        |┣━━╋━━┫     ┃   
        |┃21┃22┃23 24┃25 
        |┗━━┻━━┛  ╻  ╹  ╻
        | 26 27 28┃29 30┃
        |┏━━┳━━┓  ╹  ╻  ╹
        |┃31┃32┃33 34┃35 
        |┣━━╋━━┫  ╺━━╋━━╸
        |┃36┃37┃38 39┃40 
        |┗━━┻━━┛  ╻  ╹  ╻
        | 41 42 43┃44 45┃
        |┏━━┳━━┓  ╹  ╻  ╹
        |┃46┃47┃48 49┃50 
        |┣━━╋━━┫     ┃   
        |┃51┃52┃53 54┃55 
        |┗━━┻━━┛  ╻  ╹  ╻
        | 56 57 58┃59 60┃
        |┏━━┳━━┓  ╹  ╻  ╹
        |┃61┃62┃63 64┃65 
        |┣━━╋━━┫  ╺━━╋━━╸
        |┃66┃67┃68 69┃70 
        |┗━━┻━━┛     ╹   
        """)

    @Test
    fun double() = doTest(BorderStyle.DOUBLE, """
        |╔══╦══╗         
        |║1 ║2 ║3  4 ║5  
        |╠══╬══╣   ══╬══ 
        |║6 ║7 ║8  9 ║10 
        |╚══╩══╝         
        | 11 12 13║14 15║
        |╔══╦══╗         
        |║16║17║18 19║20 
        |╠══╬══╣     ║   
        |║21║22║23 24║25 
        |╚══╩══╝         
        | 26 27 28║29 30║
        |╔══╦══╗         
        |║31║32║33 34║35 
        |╠══╬══╣   ══╬══ 
        |║36║37║38 39║40 
        |╚══╩══╝         
        | 41 42 43║44 45║
        |╔══╦══╗         
        |║46║47║48 49║50 
        |╠══╬══╣     ║   
        |║51║52║53 54║55 
        |╚══╩══╝         
        | 56 57 58║59 60║
        |╔══╦══╗         
        |║61║62║63 64║65 
        |╠══╬══╣   ══╬══ 
        |║66║67║68 69║70 
        |╚══╩══╝         
        """)
    @Test
    fun heavy_head_foot() = doTest(BorderStyle.HEAVY_HEAD_FOOT, """
        |┏━━┳━━┓     ╻   
        |┃1 ┃2 ┃3  4 ┃5  
        |┣━━╋━━┫  ╺━━╋━━╸
        |┃6 ┃7 ┃8  9 ┃10 
        |┗━━┻━━┛  ╻  ╹  ╻
        | 11 12 13┃14 15┃
        |┏━━┳━━┓  ╹  ╻  ╹
        |┃16┃17┃18 19┃20 
        |┡━━╇━━┩     ╿   
        |│21│22│23 24│25 
        |└──┴──┘  ╷  ╵  ╷
        | 26 27 28│29 30│
        |┌──┬──┐  ╵  ╷  ╵
        |│31│32│33 34│35 
        |├──┼──┤  ╶──┼──╴
        |│36│37│38 39│40 
        |└──┴──┘  ╷  ╵  ╷
        | 41 42 43│44 45│
        |┌──┬──┐  ╵  ╷  ╵
        |│46│47│48 49│50 
        |┢━━╈━━┪     ╽   
        |┃51┃52┃53 54┃55 
        |┗━━┻━━┛  ╻  ╹  ╻
        | 56 57 58┃59 60┃
        |┏━━┳━━┓  ╹  ╻  ╹
        |┃61┃62┃63 64┃65 
        |┣━━╋━━┫  ╺━━╋━━╸
        |┃66┃67┃68 69┃70 
        |┗━━┻━━┛     ╹   
        """)

    @Test
    fun ascii() = doTest(BorderStyle.ASCII, """
        |+--+--+         
        ||1 |2 |3  4 |5  
        |+--+--+   --+-- 
        ||6 |7 |8  9 |10 
        |+--+--+         
        | 11 12 13|14 15|
        |+--+--+         
        ||16|17|18 19|20 
        |+--+--+     |   
        ||21|22|23 24|25 
        |+--+--+         
        | 26 27 28|29 30|
        |+--+--+         
        ||31|32|33 34|35 
        |+--+--+   --+-- 
        ||36|37|38 39|40 
        |+--+--+         
        | 41 42 43|44 45|
        |+--+--+         
        ||46|47|48 49|50 
        |+--+--+     |   
        ||51|52|53 54|55 
        |+--+--+         
        | 56 57 58|59 60|
        |+--+--+         
        ||61|62|63 64|65 
        |+--+--+   --+-- 
        ||66|67|68 69|70 
        |+--+--+         
        """)
    private fun doTest(borderStyle: BorderStyle, expected: String) {
        checkRender(table {
            this.borderStyle = borderStyle
            padding = Padding.none()

            var i = 1
            fun SectionBuilder.allCorners() {
                row {
                    cells(i++, i++)
                    cell(i++) { borders = NONE }
                    cell(i++) { borders = RIGHT }
                    cell(i++) { borders = BOTTOM }
                }
                row {
                    cells(i++, i++)
                    cell(i++) { borders = NONE }
                    cell(i++) { borders = TOP }
                    cell(i++) { borders = LEFT }
                }
            }

            fun SectionBuilder.emptyRow() {
                row {
                    cells(i++, i++, i++) { borders = NONE }
                    cell(i++) { borders = LEFT }
                    cell(i++) { borders = RIGHT }
                }
            }

            fun SectionBuilder.transitionRow() {
                row {
                    cells(i++, i++)
                    cell(i++) { borders = NONE }
                    cell(i++) { borders = RIGHT }
                    cell(i++) { borders = NONE }
                }
            }
            header {
                allCorners()
                emptyRow()
                transitionRow()
            }

            body {
                transitionRow()
                emptyRow()
                allCorners()
                emptyRow()
                transitionRow()
            }

            footer {
                transitionRow()
                emptyRow()
                allCorners()
            }
        }, expected)
    }
}

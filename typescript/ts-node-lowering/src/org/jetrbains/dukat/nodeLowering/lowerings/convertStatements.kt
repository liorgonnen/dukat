package org.jetrbains.dukat.nodeLowering.lowerings

import org.jetbrains.dukat.astCommon.IdentifierEntity
import org.jetbrains.dukat.astModel.TypeValueModel
import org.jetbrains.dukat.astModel.VariableModel
import org.jetbrains.dukat.astModel.expressions.AsExpressionModel
import org.jetbrains.dukat.astModel.expressions.BinaryExpressionModel
import org.jetbrains.dukat.astModel.expressions.CallExpressionModel
import org.jetbrains.dukat.astModel.expressions.ConditionalExpressionModel
import org.jetbrains.dukat.astModel.expressions.ExpressionModel
import org.jetbrains.dukat.astModel.expressions.IdentifierExpressionModel
import org.jetbrains.dukat.astModel.expressions.IndexExpressionModel
import org.jetbrains.dukat.astModel.expressions.NonNullExpressionModel
import org.jetbrains.dukat.astModel.expressions.PropertyAccessExpressionModel
import org.jetbrains.dukat.astModel.expressions.SuperExpressionModel
import org.jetbrains.dukat.astModel.expressions.ThisExpressionModel
import org.jetbrains.dukat.astModel.expressions.UnaryExpressionModel
import org.jetbrains.dukat.astModel.expressions.literals.BooleanLiteralExpressionModel
import org.jetbrains.dukat.astModel.expressions.literals.LiteralExpressionModel
import org.jetbrains.dukat.astModel.expressions.literals.NumericLiteralExpressionModel
import org.jetbrains.dukat.astModel.expressions.literals.StringLiteralExpressionModel
import org.jetbrains.dukat.astModel.expressions.operators.BinaryOperatorModel
import org.jetbrains.dukat.astModel.expressions.operators.BinaryOperatorModel.*
import org.jetbrains.dukat.astModel.expressions.operators.UnaryOperatorModel
import org.jetbrains.dukat.astModel.expressions.operators.UnaryOperatorModel.*
import org.jetbrains.dukat.astModel.expressions.templates.ExpressionTemplateTokenModel
import org.jetbrains.dukat.astModel.expressions.templates.StringTemplateTokenModel
import org.jetbrains.dukat.astModel.expressions.templates.TemplateExpressionModel
import org.jetbrains.dukat.astModel.expressions.templates.TemplateTokenModel
import org.jetbrains.dukat.astModel.modifiers.VisibilityModifierModel
import org.jetbrains.dukat.astModel.statements.BlockStatementModel
import org.jetbrains.dukat.astModel.statements.BreakStatementModel
import org.jetbrains.dukat.astModel.statements.CaseModel
import org.jetbrains.dukat.astModel.statements.ContinueStatementModel
import org.jetbrains.dukat.astModel.statements.ExpressionStatementModel
import org.jetbrains.dukat.astModel.statements.IfStatementModel
import org.jetbrains.dukat.astModel.statements.ReturnStatementModel
import org.jetbrains.dukat.astModel.statements.RunBlockStatementModel
import org.jetbrains.dukat.astModel.statements.StatementModel
import org.jetbrains.dukat.astModel.statements.WhenStatementModel
import org.jetbrains.dukat.astModel.statements.WhileStatementModel
import org.jetbrains.dukat.panic.raiseConcern
import org.jetbrains.dukat.tsmodel.BlockDeclaration
import org.jetbrains.dukat.tsmodel.BreakStatementDeclaration
import org.jetbrains.dukat.tsmodel.CaseDeclaration
import org.jetbrains.dukat.tsmodel.ContinueStatementDeclaration
import org.jetbrains.dukat.tsmodel.ExpressionStatementDeclaration
import org.jetbrains.dukat.tsmodel.IfStatementDeclaration
import org.jetbrains.dukat.tsmodel.ReturnStatementDeclaration
import org.jetbrains.dukat.tsmodel.StatementDeclaration
import org.jetbrains.dukat.tsmodel.SwitchStatementDeclaration
import org.jetbrains.dukat.tsmodel.VariableDeclaration
import org.jetbrains.dukat.tsmodel.WhileStatementDeclaration
import org.jetbrains.dukat.tsmodel.expression.AsExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.BinaryExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.CallExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.ConditionalExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.ElementAccessExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.ExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.NewExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.NonNullExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.PropertyAccessExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.UnaryExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.UnknownExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.literal.BooleanLiteralExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.literal.LiteralExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.literal.NumericLiteralExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.literal.StringLiteralExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.name.IdentifierExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.name.QualifierExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.templates.ExpressionTemplateTokenDeclaration
import org.jetbrains.dukat.tsmodel.expression.templates.StringTemplateTokenDeclaration
import org.jetbrains.dukat.tsmodel.expression.templates.TemplateExpressionDeclaration
import org.jetbrains.dukat.tsmodel.expression.templates.TemplateTokenDeclaration

internal class ExpressionConverter(val documentConverter: DocumentConverter) {
    private fun LiteralExpressionDeclaration.convert(): LiteralExpressionModel {
        return when (this) {
            is StringLiteralExpressionDeclaration -> StringLiteralExpressionModel(
                value
            )
            is NumericLiteralExpressionDeclaration -> NumericLiteralExpressionModel(
                value.toInt()
            )
            is BooleanLiteralExpressionDeclaration -> BooleanLiteralExpressionModel(
                value
            )
            else -> raiseConcern("unable to process LiteralExpressionDeclaration $this") {
                StringLiteralExpressionModel("ERROR")
            }
        }
    }

    private fun TemplateTokenDeclaration.convert(): TemplateTokenModel {
        return when (this) {
            is StringTemplateTokenDeclaration -> StringTemplateTokenModel(value.convert() as StringLiteralExpressionModel)
            is ExpressionTemplateTokenDeclaration -> ExpressionTemplateTokenModel(expression.convert())
            else -> raiseConcern("unable to process TemplateTokenDeclaration $this") {
                StringTemplateTokenModel(StringLiteralExpressionModel("ERROR"))
            }
        }
    }

    private fun convertBinaryOperator(operator: String): BinaryOperatorModel {
        return when (operator) {
            "+" -> PLUS
            "-" -> MINUS
            "*" -> MULT
            "/" -> DIV
            "%" -> MOD
            "=" -> ASSIGN
            "+=" -> PLUS_ASSIGN
            "-=" -> MINUS_ASSIGN
            "*=" -> MULT_ASSIGN
            "/=" -> DIV_ASSIGN
            "%=" -> MOD_ASSIGN
            "&&" -> AND
            "||" -> OR
            "==" -> EQ
            "!=" -> NOT_EQ
            "===" -> REF_EQ
            "!==" -> REF_NOT_EQ
            "<" -> LT
            ">" -> GT
            "<=" -> LE
            ">=" -> GE
            else -> raiseConcern("unable to process binary operator $this") {
                PLUS
            }
        }
    }

    private fun convertUnaryOperator(operator: String): UnaryOperatorModel {
        return when (operator) {
            "++" -> INCREMENT
            "--" -> DECREMENT
            "+" -> UNARY_PLUS
            "-" -> UNARY_MINUS
            "!" -> NOT
            else -> raiseConcern("unable to process unary operator $this") {
                INCREMENT
            }
        }
    }

    private fun ExpressionDeclaration.convert(): ExpressionModel {
        return when (this) {
            is IdentifierExpressionDeclaration -> IdentifierExpressionModel(
                identifier
            )
            is QualifierExpressionDeclaration -> {
                IdentifierExpressionModel(
                    qualifier
                )
            }
            is CallExpressionDeclaration -> CallExpressionModel(
                expression.convert(),
                arguments.map { it.convert() }
            )
            is NewExpressionDeclaration -> CallExpressionModel(
                expression.convert(),
                arguments.map { it.convert() }
            )
            is PropertyAccessExpressionDeclaration -> PropertyAccessExpressionModel(
                expression.convert(),
                IdentifierExpressionModel(name)
            )
            is ElementAccessExpressionDeclaration -> IndexExpressionModel(
                expression.convert(),
                argumentExpression.convert()
            )
            is LiteralExpressionDeclaration -> this.convert()
            is TemplateExpressionDeclaration -> TemplateExpressionModel(
                tokens.map { it.convert() }
            )
            is BinaryExpressionDeclaration -> BinaryExpressionModel(
                left.convert(),
                convertBinaryOperator(operator),
                right.convert()
            )
            is UnaryExpressionDeclaration -> UnaryExpressionModel(
                operand.convert(),
                convertUnaryOperator(operator),
                isPrefix
            )
            is ConditionalExpressionDeclaration -> ConditionalExpressionModel(
                condition.convert(),
                whenTrue.convert(),
                whenFalse.convert()
            )
            is AsExpressionDeclaration -> AsExpressionModel(
                expression.convert(),
                TypeValueModel(
                    IdentifierEntity("String"),
                    listOf(),
                    null,
                    null
                )
                //TODO
                /*with (documentConverter) {
                    type.process()
                }*/
            )
            is NonNullExpressionDeclaration -> NonNullExpressionModel(
                expression.convert()
            )
            is UnknownExpressionDeclaration -> when (meta) {
                "this" -> ThisExpressionModel()
                "super" -> SuperExpressionModel()
                else -> raiseConcern("unable to process ExpressionDeclaration $this") {
                    IdentifierExpressionModel(IdentifierEntity(meta))
                }
            }
            else -> raiseConcern("unable to process ExpressionDeclaration $this") {
                IdentifierExpressionModel(IdentifierEntity("ERROR"))
            }
        }
    }

    private fun BlockDeclaration.isFallthroughBlock(): Boolean {
        return when (statements.lastOrNull()) {
            is BreakStatementDeclaration -> false
            else -> true
        }
    }

    private fun SwitchStatementDeclaration.splitToFallthroughBlocks(): List<List<CaseDeclaration>> {
        val fallthroughBlocks = mutableListOf<List<CaseDeclaration>>()
        var currentBlock = mutableListOf<CaseDeclaration>()
        cases.forEach {
            currentBlock.add(it)
            if (!it.body.isFallthroughBlock()) {
                fallthroughBlocks += currentBlock
                currentBlock = mutableListOf()
            }
        }
        if (currentBlock.isNotEmpty()) {
            fallthroughBlocks += currentBlock
        }
        return fallthroughBlocks
    }

    private fun StatementModel.removeBreaks(): StatementModel? {
        return when (this) {
            is BlockStatementModel -> removeBreaks()
            is BreakStatementModel -> null
            is IfStatementModel -> copy(
                thenStatement = thenStatement.removeBreaks(),
                elseStatement = elseStatement?.removeBreaks()
            )
            is RunBlockStatementModel -> copy(statements = statements.mapNotNull { it.removeBreaks() })
            is WhenStatementModel -> copy(cases = cases.map { it.copy(body = it.body.removeBreaks()) })
            is WhileStatementModel -> copy(body = body.removeBreaks())
            else -> this
        }
    }

    private fun BlockStatementModel.removeBreaks() = copy(statements = statements.mapNotNull { it.removeBreaks() })

    private fun List<CaseDeclaration>.convert(expressionToCompare: ExpressionModel): CaseModel? {
        return when (this.size) {
            0 -> null
            1 -> CaseModel(
                first().condition?.convert()?.let { listOf(it) },
                convertBlock(first().body)
            )
            else -> {
                val condition = if (none { it.condition == null }) {
                    mapNotNull { it.condition?.convert() }
                } else {
                    null
                }
                val body = map { case ->
                    if (case.condition == null) {
                        case.body.statements.map { it.convert() }
                    } else {
                        listOf(
                            IfStatementModel(
                                BinaryExpressionModel(
                                    expressionToCompare,
                                    EQ,
                                    case.condition!!.convert()
                                ),
                                convertBlock(case.body),
                                null
                            )
                        )
                    }
                }.flatten()
                CaseModel(
                    condition,
                    BlockStatementModel(body)
                )
            }
        }
    }

    private fun SwitchStatementDeclaration.convert(): WhenStatementModel {
        val expressionToCompare = expression.convert()

        return WhenStatementModel(
            expressionToCompare,
            splitToFallthroughBlocks()
                .mapNotNull {
                    val caseModel = it.convert(expressionToCompare)
                    caseModel?.copy(body = caseModel.body.removeBreaks())
                }
        )
    }

    private fun StatementDeclaration.convert(): StatementModel {
        return when (this) {
            is ExpressionStatementDeclaration -> ExpressionStatementModel(
                expression.convert()
            )
            is ReturnStatementDeclaration -> ReturnStatementModel(
                expression?.convert()
            )
            is BreakStatementDeclaration -> BreakStatementModel()
            is ContinueStatementDeclaration -> ContinueStatementModel()
            is IfStatementDeclaration -> IfStatementModel(
                condition.convert(),
                convertBlock(thenStatement),
                elseStatement?.let { convertBlock(it) }
            )
            is WhileStatementDeclaration -> WhileStatementModel(
                condition.convert(),
                convertBlock(statement)
            )
            is SwitchStatementDeclaration -> convert()
            is BlockDeclaration -> RunBlockStatementModel(
                statements.map { it.convert() }
            )
            is VariableDeclaration -> VariableModel(
                name = IdentifierEntity(name),
                type = TypeValueModel(
                    IdentifierEntity("Number"),
                    listOf(),
                    null,
                    null
                )
                //TODO
                /*with (documentConverter) {
                    type.process()
                }*/,
                annotations = mutableListOf(),
                immutable = false,
                inline = false,
                external = false,
                initializer = initializer?.convert()?.let {
                    ExpressionStatementModel(it)
                },
                get = null,
                set = null,
                typeParameters = listOf(),
                extend = null,
                visibilityModifier = VisibilityModifierModel.DEFAULT,
                comment = null
            )
            else -> raiseConcern("unable to process StatementDeclaration $this") {
                ExpressionStatementModel(
                    IdentifierExpressionModel(IdentifierEntity("ERROR"))
                )
            }
        }
    }

    fun convertBlock(blockDeclaration: BlockDeclaration): BlockStatementModel {
        return BlockStatementModel(blockDeclaration.statements.map { it.convert() })
    }
}
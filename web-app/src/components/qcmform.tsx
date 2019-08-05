import React from "react";
import FormComponent from "../common/form/formComponent";
import {Choice, Qcm, Question} from "../types";
import {toast} from "react-toastify";
import {isEmpty} from "../common/util/functions";
import Checkbox from "./checkbox";

type State = {
    form: Qcm,
    errors: any
};
type Props = {
    onSubmit(qcm: Qcm): void,
    onCancel(): void,
    qcm: Qcm // initial qcm
};

// negative ids means manually created object
class QcmForm extends FormComponent<Props, Qcm> {

    state: State;

    constructor(props: Props) {
        super(props);

        let form: Qcm = {...this.props.qcm};
        if (!form.questions.length) {
            form.questions.push({
                id: - form.questions.length - 1,
                question: "",
                choices: []
            });
        }
        for (let q of form.questions) {
            if (!q.choices.length) {
                q.choices.push({
                    id: -q.choices.length,
                    value: "",
                    answer: false
                })
            }
        }
        this.state = {
            form,
            errors: {

            }
        };

    }

    render(): React.ReactElement {
        const {form:qcm} = this.state;
        let questionsElements: React.ReactElement[] = [];
        let i: number = 0;
        for (let q of qcm.questions) {
            questionsElements.push(this.renderQuestion(q, i++))
        }
        return (
            <React.Fragment>
                {this.renderInput({placeholder: "Enter the title of the qcm", type: "", name: "name", autoFocus: true})}

                {questionsElements}

                <button
                    style={{
                        margin: 20
                    }}
                    onClick={() => {
                        let qcm: Qcm = {...this.state.form};
                        qcm.questions.push({
                            id: -qcm.questions.length,
                            question: "",
                            choices: [{
                                id: 0,
                                value: "",
                                answer: false
                            }]
                        });
                        this.setState({
                            form: qcm
                        });
                    }}
                >Add question</button>


                <div
                    style={{
                        margin: 40
                    }}
                >
                    <button
                        onClick={this.props.onCancel}
                    >Cancel</button>

                    <button
                        className="center-horizontal"
                        onClick={this.onSubmit}
                    >Save</button>
                </div>
            </React.Fragment>
        );
    }

    onSubmit = (): void => {
        const {form} = this.state;
        const errors = this.validate(form);
        this.setState({errors});
        if (isEmpty(errors)) {
            this.props.onSubmit(form);
        }
    };

    validate = (form: Qcm): any => {
        if (!form.questions.length) {
            toast.error("You should create at least one question");
            return {notEmpty: true};
        }
        let errors: any = {};
        if (!form.name) {
            errors.name = "You should provide a title";
        }
        for(let i=0; i < form.questions.length; i++) {
            const q: Question = form.questions[i];
            if (!q.question.length) {
                errors[`qName${i}`] = 'You should provide a question';
            }
            if (!q.choices.length) {
                toast.error(`Question n°${i + 1} don't have any choices`);
                errors.hasError = true;
            } else {
                if (!q.choices.filter(q => q.answer).length) {
                    toast.error(`Question n°${i + 1} don't have right answers`);
                    errors.hasError = true;
                }
                for (let j=0; j< q.choices.length; j++) {
                    if (!q.choices[j].value) {
                        errors[`q${i}Choice${j}`] = 'You should provide a name';
                    }
                }
            }
        }
        return errors;
    };

    validateProperty(form: any, name: string): any {
       //never called
    }

    private renderQuestion = (q: Question, i: number): React.ReactElement => {
        let choicesElements: React.ReactElement[] = [];
        let j: number = 0;
        for (let c of q.choices) {
            choicesElements.push(this.renderChoice(q, c, i, j++));
        }
        return <div
            key={q.id}
            style={{
                background: i % 2 ? '#c6c6c6': '#ffffff'
            }}
        >
            {this.renderInput({placeholder: "Enter the title of the question", type: "", label: `Question ${i + 1}`, name: `qName${i}`,
                valueLoader:() => q.question,
                width: '80%',
                onChange: (event: any): any => {
                let qcm: Qcm = {...this.state.form};
                    qcm.questions.filter(que => q.id === que.id)[0].question = event.target.value;
                this.setState({
                    form: qcm
                });
                }})}

            <div
                className="choices-view-grid"
            >
                {choicesElements}

                <div
                    className="full-height full-width"
                >
                    <button
                        className="center-fixed-width"
                        style={{
                            width: 100,
                            height: 30
                        }}
                        onClick={() => {
                            let qcm: Qcm = {...this.state.form};
                            let question: Question = qcm.questions.filter(que => q.id  === que.id)[0];
                            question.choices.push({
                                id: - question.choices.length - 1,
                                value: "",
                                answer: false
                            });
                            this.setState({
                                form: qcm
                            });
                        }}
                    >Add choice</button>
                </div>
            </div>
        </div>
    };

    // id = index
    private renderChoice = (q: Question, c: Choice, i: number, j: number): React.ReactElement => { //TODO render checkbox for is answer
        return (
            <div
                key={c.id.toString()}
            >
                {this.renderInput({placeholder: "Enter the name of the choice", type: "", label: `Choice ${j + 1}`, name: `q${i}Choice${j}`,
                    width: '80%',
                    valueLoader: () => c.value,
                    onChange: (event: any): any => {
                        let qcm: Qcm = {...this.state.form};
                        let question: Question = qcm.questions.filter(que => q.id  === que.id)[0];
                        let choice: Choice = question.choices.filter(ch => c.id === ch.id)[0];
                        choice.value = event.target.value;
                        this.setState({
                            form: qcm
                        });
                    }})}

                <div>
                    <p
                        className="inline"
                    >Right answer: </p>
                    <Checkbox
                        className="inline"
                        checked={c.answer} onValueChange={() => {
                        let qcm: Qcm = {...this.state.form};
                        let question: Question = qcm.questions.filter(que => q.id  === que.id)[0];
                        let choice: Choice = question.choices.filter(ch => c.id === ch.id)[0];
                        choice.answer = !choice.answer;
                        this.setState({
                            form: qcm
                        });
                    }} />
                </div>
            </div>
        )
    }


}

export default QcmForm;
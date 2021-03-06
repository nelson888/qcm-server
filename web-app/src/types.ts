
export type Choice = {
    id: number,
    answer: boolean,
    value: string
}
export type Question = {
    id: number,
    choices: Choice[],
    question: string
};

export type QcmState = 'COMPLETE' | 'INCOMPLETE' | 'STARTED' | 'FINISHED';

export type Qcm = {
    id: number,
    name: string,
    state: QcmState,
    questions: Question[]
};

export type QuestionResult = {
    question: Question,
    responses: { [username:string]: boolean }
};

export type QcmResult = {
    participants: string[],
    questionResults: QuestionResult[]
};

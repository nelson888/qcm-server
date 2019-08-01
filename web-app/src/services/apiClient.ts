import {Qcm} from "../types";
import {
    APIResponse,
    login_response,
    LoginResponse, MeResponse, QcmAllResponse,
    QcmClient, Role,
    STUDENT,
    User
} from "./qcmClient";

const API_URL = 'http://localhost:8080';

class ApiClient implements QcmClient {

    private user: User = {
        username: "",
        role: STUDENT,
    };
    private jwt: string = "";

    private getHeaders = (): any => {
        let headers: any = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (this.isLogged()) {
            headers['Authorization'] = `Bearer ${this.jwt}`;
        }
        return headers;
    };

    private post(endpoint: string, data: any): Promise<Response> {
        return fetch(API_URL + endpoint,
            {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(data)
            });
    }

    private get(endpoint: string): Promise<Response> {
        return fetch(API_URL + endpoint,
            {
                method: 'GET',
                headers: this.getHeaders(),
            });
    }

    private async extractErrorMessage(response: Response): Promise<string> {
        let errorResponse: any =  await response.json();
        return errorResponse.message;
    }

    private isError(response: Response): boolean {
        return response.status < 200 || response.status >= 300;
    }

    private async errorResponse<T>(response: Response): Promise<APIResponse<T, string>> {
        return new APIResponse({
            isSuccess: false,
            errorData: await this.extractErrorMessage(response)
        });
    }
    logIn = async (username: string, password: string): Promise<LoginResponse> =>  {
        let response: Response = await this.post('/auth/login',
            {
                username,
                password
            });

        if (this.isError(response)) {
            return this.errorResponse<login_response>(response);
        }
        let json: login_response = await response.json();
        this.jwt = json.jwt;
        this.user = {...json};

        return new APIResponse({
            isSuccess: true,
            successData: {...json}
        });
    };

    isLogged = (): boolean => {
        return !!this.jwt;
    };

    getRole = (): Role => {
        return this.user.role;
    };

    async getQcms(): Promise<QcmAllResponse> {
        let response: Response = await this.get('/qcm/all');
        if (this.isError(response)) {
            return await this.errorResponse<Qcm[]>(response);
        }
        let jsonList: Qcm[] = await response.json();
        return new APIResponse({
            isSuccess: true,
            successData: jsonList
        });
    }

    async getMe(): Promise<MeResponse> {
        let response: Response = await this.get('/users/me');
        if (this.isError(response)) {
            return await this.errorResponse<User>(response);
        }
        let json: User = await response.json();
        this.user = {...json};
        return new APIResponse({
            isSuccess: true,
            successData: {...json}
        });
    }

    setUser(user: User): void {
        this.user = user;
    }

    setJwt(jwt: string): void {
        this.jwt = jwt;
    }
}

export default ApiClient;

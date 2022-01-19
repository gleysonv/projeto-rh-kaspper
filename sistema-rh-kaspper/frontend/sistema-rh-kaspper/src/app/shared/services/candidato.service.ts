import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CandidatoDto} from "../models/dto/candidato-dto";
import {Observable} from "rxjs";

const urlBase = 'http://localhost:8080/api/v1';
const path = 'candidatos';

@Injectable({
  providedIn: 'root'
})
export class CandidatoService {

  constructor(protected http: HttpClient) {
  }

  salvar(candidato: CandidatoDto): Observable<CandidatoDto> {
    return this.http.post<CandidatoDto>(`${urlBase}/${path}`, candidato);
  }

  getCandidatos(): Observable<CandidatoDto[]> {
    return this.http.get<CandidatoDto[]>(`${urlBase}/${path}`);
  }
}

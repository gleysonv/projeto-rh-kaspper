import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CandidatoDto} from "../models/dto/candidato-dto";
import {Observable} from "rxjs";
import {PerfilDto} from "../models/dto/perfil-dto";

const urlBase = 'http://localhost:8080/api/v1';
const path = 'perfis';

@Injectable({
  providedIn: 'root'
})
export class PerfilService {

  constructor(protected http: HttpClient) {
  }

  salvar(perfil: PerfilDto): Observable<PerfilDto> {
    return this.http.post<PerfilDto>(`${urlBase}/${path}`, perfil);
  }

  getPerfis(): Observable<PerfilDto[]> {
    return this.http.get<PerfilDto[]>(`${urlBase}/${path}`);
  }
}

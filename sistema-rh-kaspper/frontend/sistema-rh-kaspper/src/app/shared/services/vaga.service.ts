import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {VagaDto} from "../models/dto/vaga-dto";

const urlBase = 'http://localhost:8080/api/v1';
const path = 'vagas';

@Injectable({
  providedIn: 'root'
})
export class VagaService {

  constructor(protected http: HttpClient) {
  }

  salvar(vaga: VagaDto): Observable<VagaDto> {
    return this.http.post<VagaDto>(`${urlBase}/${path}`, vaga);
  }

}

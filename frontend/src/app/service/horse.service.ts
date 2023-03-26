import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Horse, HorseSearch, HorseTree } from '../dto/horse';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root',
})
export class HorseService {
  constructor(private http: HttpClient) {}

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
  }

  search(searchParams: HorseSearch): Observable<Horse[]> {
    console.log(searchParams);
    let params = new HttpParams();

    if (searchParams.name) params = params.set('name', searchParams.name);
    if (searchParams.description)
      params = params.set('description', searchParams.description);
    if (searchParams.bornBefore)
      params = params.set('bornBefore', searchParams.bornBefore.toString());
    if (searchParams.ownerName)
      params = params.set('ownerName', searchParams.ownerName);
    if (searchParams.sex) params = params.set('sex', searchParams.sex);
    if (searchParams.limit) params = params.set('limit', searchParams.limit);

    console.log(params);

    return this.http.get<Horse[]>(baseUri, { params });
  }

  public searchByName(name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams().set('name', name).set('limit', limitTo);
    return this.http.get<Horse[]>(baseUri, { params });
  }

  public getGenerationById(
    id: string | undefined,
    limit: number
  ): Observable<HorseTree> {
    const params = new HttpParams().set('limit', limit);

    return this.http.get<HorseTree>(`${baseUri}/${id}/generations`, { params });
  }

  /**
   * Get horse with specified id
   *
   * @return observable for the specified horse.
   */
  getById(id: number | undefined): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`);
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(baseUri, horse);
  }

  /**
   * Updates an existing horse in the system
   *
   * @param id the id of the horse that should be updated
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(`${baseUri}/${horse.id}`, horse);
  }

  /**
   * Deletes an existing horse from the system
   *
   * @param horse the data for the horse that should be deleted
   * @return an Observable for the deleted horse
   */
  delete(id: number | undefined): Observable<Horse> {
    return this.http.delete<Horse>(`${baseUri}/${id}`);
  }
}

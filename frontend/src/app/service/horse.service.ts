import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse} from '../dto/horse';
import {Sex} from '../dto/sex';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
  ) { }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
  }

  /**
   * Get horse with specified id
   *
   * @return observable for the specified horse.
   */
  getById(id: string | null): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`);
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  /**
   * Updates an existing horse in the system
   *
   * @param id the id of the horse that should be updated
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(
      `${baseUri}/${horse.id}`,
      horse
    );
  }

    /**
   * Deletes an existing horse from the system
   *
   * @param horse the data for the horse that should be deleted
   * @return an Observable for the deleted horse
   */
  delete(horse: Horse): Observable<Horse> {
    return this.http.delete<Horse>(
      `${baseUri}/${horse.id}`
    );
  }

}

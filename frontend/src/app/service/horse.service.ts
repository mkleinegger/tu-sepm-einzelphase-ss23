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
   * Get all horses stored in the system, which match the searchParams
   *
   * @param searchParams all search criteria, to match the horses
   * @returns an Observable of Horses
   */
  search(searchParams: HorseSearch): Observable<Horse[]> {
    let params = new HttpParams();
    if (searchParams.name) {
      params = params.set('name', searchParams.name);
    }
    if (searchParams.description) {
      params = params.set('description', searchParams.description);
    }
    if (searchParams.bornBefore) {
      params = params.set('bornBefore', searchParams.bornBefore.toString());
    }
    if (searchParams.ownerName) {
      params = params.set('ownerName', searchParams.ownerName);
    }
    if (searchParams.sex) {
      params = params.set('sex', searchParams.sex);
    }
    if (searchParams.limit) {
      params = params.set('limit', searchParams.limit);
    }

    return this.http.get<Horse[]>(baseUri, { params });
  }

  /**
   * Gets maximal the number of horses, specified in limitTo, which are matching the name
   *
   * @param name Name to search to
   * @param limitTo number of Horses to load, even if more are available
   * @returns  an Observable of Horses
   */
  public searchByName(name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams().set('name', name).set('limit', limitTo);
    return this.http.get<Horse[]>(baseUri, { params });
  }

  /**
   * Gets an family-tree containing having a depth of maximal the limit specified
   *
   * @param id the id of the horse from which the tree should be loaded
   * @param limit number of generations, which should be loaded
   * @returns observable for the loaded family-tree
   */
  public getFamilyTree(
    id: number | undefined,
    limit: number
  ): Observable<HorseTree> {
    const params = new HttpParams().set('numberOfGenerations', limit);

    return this.http.get<HorseTree>(`${baseUri}/${id}/familytree`, { params });
  }

  /**
   * Get horse with specified id
   *
   * @param id the id of the horse that should be loaded
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
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(`${baseUri}/${horse.id}`, horse);
  }

  /**
   * Deletes an existing horse from the system
   *
   * @param id he id of the horse that should be deleted
   * @return an Observable for the deleted horse
   */
  delete(id: number | undefined): Observable<Horse> {
    return this.http.delete<Horse>(`${baseUri}/${id}`);
  }
}

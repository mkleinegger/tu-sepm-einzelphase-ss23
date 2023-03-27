import { NgModel } from '@angular/forms';
import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, Router } from '@angular/router';
import { HorseService } from 'src/app/service/horse.service';
import { HorseTree, Horse } from '../../../dto/horse';
import { EMPTY, Observable, throwError } from 'rxjs';
import { catchError, filter } from 'rxjs/operators';

@Component({
  selector: 'app-family-tree-view',
  templateUrl: './family-tree-view.component.html',
  styleUrls: ['./family-tree-view.component.scss'],
})
export class FamilyTreeViewComponent implements OnInit {
  horseTree$: Observable<HorseTree> = new Observable<HorseTree>();
  id = -1;
  limitOfGenerations = 0;

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams
      .pipe(filter((params) => params.generations))
      .subscribe((params) => {
        this.limitOfGenerations = params.generations;
      });

    this.route.paramMap.subscribe((paramMap) => {
      //id should always be present, because if not this route is not accesible
      this.id = Number(paramMap.get('id'));
      if (isNaN(this.id)) {
        this.notification.error(
          `routeparam :id =${paramMap.get('id')} is not valid`
        );
        this.router.navigate(['/horses']);
      }

      if (this.limitOfGenerations > 0) {
        this.reloadFamilyTree();
      }
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid,
    };
  }

  reloadFamilyTree(): void {
    this.horseTree$ = this.service
      .getFamilyTree(this.id, this.limitOfGenerations)
      .pipe(
        catchError((err) => {
          this.errorHandler(err, 'fetch');
          return EMPTY;
        })
      );
  }

  public deleteHorse(horse: HorseTree) {
    if (horse != null) {
      const observable: Observable<Horse> = this.service.delete(horse.id);
      observable.subscribe({
        next: () => {
          this.notification.success(`Horse ${horse.name} successfully deleted`);

          // if root is deleted route back to home
          if (horse.id === this.id) {
            this.router.navigate(['/horses']);
          } else {
            this.reloadFamilyTree();
          }
        },
        error: (error) => {
          console.error('Error creating horse', error);
          this.errorHandler(error, 'deleted');
        },
      });
    }
  }

  private errorHandler(
    error: any,
    action: string | undefined = undefined
  ): void {
    if (error.status === 0) {
      this.notification.error('Backend is not reachable');
    } else if (error.status === 404) {
      this.notification.error(
        `Could not ${action} horse - ` + error.error.error
      );
      this.router.navigate(['/horses']);
    } else if (error.status === 409 || error.status === 422) {
      for (const err of error.error.errors) {
        this.notification.error(err);
      }
    } else if (error.status >= 500) {
      this.notification.error(
        error.error.error + ': Something unexpected happend!'
      );
    }
  }
}

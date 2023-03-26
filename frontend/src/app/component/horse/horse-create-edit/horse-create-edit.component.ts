import { Component, OnInit } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { Horse, HorseSearch } from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { Sex } from 'src/app/dto/sex';
import { HorseService } from 'src/app/service/horse.service';
import { OwnerService } from 'src/app/service/owner.service';

export enum HorseCreateEditMode {
  create,
  edit,
}

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss'],
})
export class HorseCreateEditComponent implements OnInit {
  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: undefined,
    father: undefined,
  };
  search: HorseSearch = {
    name: undefined,
    limit: 5,
  };

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService
  ) {}

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      default:
        return 'Edit Horse';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      default:
        return 'Save';
    }
  }

  public get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      default:
        return 'edited';
    }
  }

  ownerSuggestions = (input: string) =>
    input === '' ? of([]) : this.ownerService.searchByName(input, 5);

  horseSuggestions = (input: string) =>
    input === '' ? of([]) : this.service.searchByName(input, 5);

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.mode = data.mode;
    });

    if (this.mode === HorseCreateEditMode.edit) {
      this.route.paramMap.subscribe((paramMap) => {
        //id should always be present, because if not this route is not accesible
        const id = Number(paramMap.get('id'));
        if (isNaN(id)) {
          this.notification.error(
            `routeparam :id =${paramMap.get('id')} is not valid`
          );
          this.router.navigate(['/horses']);
        }

        const observable: Observable<Horse> = this.service.getById(id);
        observable.subscribe({
          next: (data: Horse) => {
            this.horse = data;
          },
          error: (error) => {
            console.error('Error getting horse', error);
            this.errorHandler(error, 'get');
            this.router.navigate(['/horses']);
          },
        });
      });
    }
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null): string {
    return owner == null ? '' : `${owner.firstName} ${owner.lastName}`;
  }

  public formatHorseName(horse: Horse | null): string {
    return horse == null ? '' : horse.name;
  }

  public deleteHorse(horse: Horse) {
    if (horse != null) {
      const observable: Observable<Horse> = this.service.delete(horse.id);
      observable.subscribe({
        next: (data) => {
          this.notification.success(`Horse ${horse.name} successfully deleted`);
          this.router.navigate(['/horses']);
        },
        error: (error) => {
          console.error('Error deleting horse', error);
          this.errorHandler(error, 'delete');
        },
      });
    }
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(this.horse);
          break;
        case HorseCreateEditMode.edit:
          observable = this.service.update(this.horse);
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: (data) => {
          this.notification.success(
            `Horse ${this.horse.name} successfully ${this.modeActionFinished}.`
          );
          this.router.navigate(['/horses']);
        },
        error: (error) => {
          console.error(`Error ${this.modeActionFinished} horse`, error);
          this.errorHandler(error, 'update');
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

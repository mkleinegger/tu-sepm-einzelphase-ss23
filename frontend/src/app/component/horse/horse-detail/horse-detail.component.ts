import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { Horse } from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { Sex } from 'src/app/dto/sex';
import { HorseService } from 'src/app/service/horse.service';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss'],
})
export class HorseDetailComponent implements OnInit {
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: undefined,
    father: undefined,
  };

  constructor(
    private service: HorseService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
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

  public formatOwnerName(owner: Owner | null): string {
    if (owner == null) {
      return '';
    }

    const name = `${owner.firstName} ${owner.lastName}`;
    return name.length >= 50 ? name.substring(0, 50) + '...' : name;
  }

  public formatHorseName(horse: Horse | null): string {
    if (horse == null) {
      return '';
    }

    return horse?.name.length >= 50
      ? horse.name.substring(0, 50) + '...'
      : horse.name;
  }

  public formatSex(horse: Horse | null): string {
    return horse == null
      ? ''
      : horse.sex.charAt(0) + horse.sex.substring(1).toLowerCase();
  }

  public deleteHorse(horse: Horse | null) {
    if (horse != null) {
      const observable: Observable<Horse> = this.service.delete(horse.id);
      observable.subscribe({
        next: (data) => {
          this.notification.success(`Horse ${horse.name} successfully deleted`);
          this.router.navigate(['/horses']);
        },
        error: (error) => {
          console.error('Error creating horse', error);
          this.errorHandler(error, 'delete');
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

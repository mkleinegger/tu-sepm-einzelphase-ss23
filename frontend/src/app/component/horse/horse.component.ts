import { Component, OnInit, OnDestroy } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { HorseService } from 'src/app/service/horse.service';
import { OwnerService } from './../../service/owner.service';
import { Horse, HorseSearch } from '../../dto/horse';
import { Owner } from '../../dto/owner';
import { Observable, Subject, Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/operators';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss'],
})
export class HorseComponent implements OnInit, OnDestroy {
  horses: Horse[] = [];
  updateQueue = new Subject();
  updateQueueSubscription: Subscription | null = null;
  horseSearch: HorseSearch = {
    name: undefined,
    description: undefined,
    bornBefore: undefined,
    sex: undefined,
    ownerName: undefined,
    limit: undefined,
  };

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.updateQueueSubscription = this.updateQueue
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe({
        next: () => {
          this.reloadHorses();
        },
        error: () => {
          console.error('Something unexpected happened');
          this.notification.error('Something unexpected happened');
        },
      });
    this.reloadHorses();
  }

  ngOnDestroy() {
    this.updateQueueSubscription?.unsubscribe();
  }

  ownerSuggestions = (input: string) =>
    input === ''
      ? of([])
      : this.ownerService
          .searchByName(input, 5)
          .pipe(map((data) => data.map((o) => this.ownerName(o))));

  public deleteHorse(horse: Horse | null) {
    if (horse != null) {
      const observable: Observable<Horse> = this.service.delete(horse.id);
      observable.subscribe({
        next: () => {
          this.notification.success(`Horse ${horse.name} successfully deleted`);
          this.reloadHorses();
        },
        error: (error) => {
          console.error('Error deleting horse', error);
          this.errorHandler(error, 'delete');
        },
      });
    }
  }

  reloadHorses() {
    this.service.search(this.horseSearch).subscribe({
      next: (data) => {
        this.horses = data;
      },
      error: (error) => {
        console.error('Error fetching horses', error);
        this.errorHandler(error);
      },
    });
  }

  ownerName(owner: Owner | null): string {
    return owner ? `${owner.firstName} ${owner.lastName}` : '';
  }

  formatText(text: string | null, numberOfChars: number): string {
    if (text == null || numberOfChars === 0) {
      return '';
    }

    return text.length >= numberOfChars
      ? text.substring(0, numberOfChars) + '...'
      : text;
  }

  formatSex(horse: Horse | null): string {
    return horse == null
      ? ''
      : horse.sex.charAt(0) + horse.sex.substring(1).toLowerCase();
  }

  formatOwnerName(ownerName: string): string {
    return ownerName;
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
